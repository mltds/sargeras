package org.mltds.sargeras.server.facade.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.mltds.sargeras.common.core.SagaContextBase;
import org.mltds.sargeras.common.core.SagaStatus;
import org.mltds.sargeras.common.exception.SagaException;
import org.mltds.sargeras.server.dal.mapper.ContextInfoMapper;
import org.mltds.sargeras.server.dal.mapper.ContextLockMapper;
import org.mltds.sargeras.server.dal.mapper.ContextMapper;
import org.mltds.sargeras.server.dal.model.ContextDO;
import org.mltds.sargeras.server.dal.model.ContextInfoDO;
import org.mltds.sargeras.server.dal.model.ContextLockDO;
import org.mltds.sargeras.server.facade.ServerFacade;
import org.mltds.sargeras.server.service.SerializeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

/**
 * @author sunyi
 */
@Service
public class ServerFacadeImpl implements ServerFacade {

    private static final Logger logger = LoggerFactory.getLogger(ServerFacadeImpl.class);

    @Autowired
    private ContextMapper contextMapper;
    @Autowired
    private ContextInfoMapper contextInfoMapper;
    @Autowired
    private ContextLockMapper contextLockMapper;
    @Autowired
    private SerializeService serializeService;

    @Override
    public void saveContextAndLock(SagaContextBase context, int lockTimeout) {

        ContextDO contextDO = sagaContextToContextDO(context);
        contextDO.setCreateTime(new Date());
        contextDO.setModifyTime(new Date());
        contextMapper.insert(contextDO);

        Long id = contextDO.getId();
        context.setId(id);

        String triggerId = context.getTriggerId();
        lock(id, triggerId, lockTimeout);

    }

    @Override
    public SagaContextBase loadContext(long contextId) {

        ContextDO contextDO = contextMapper.selectById(contextId);
        if (contextDO == null) {
            throw new SagaException("查找Context失败，ID: " + contextId);
        }

        return contextDOToSagaContext(contextDO);

    }

    @Override
    public SagaContextBase loadContext(String appName, String bizName, String bizId) {

        ContextDO contextDO = contextMapper.selectByBiz(appName, bizName, bizId);
        if (contextDO == null) {
            throw new SagaException("查找Context失败，appName: " + appName + "，bizName：" + bizName + "，bizId：" + bizId);
        }

        return contextDOToSagaContext(contextDO);

    }

    @Override
    public void saveContextStatus(long contextId, SagaStatus status) {
        ContextDO contextDO = new ContextDO();
        contextDO.setId(contextId);
        contextDO.setStatus(status);
        contextDO.setModifyTime(new Date());
        contextMapper.updateById(contextDO);
    }

    @Override
    public void saveCurrentTx(long contextId, String currentTxName) {
        ContextDO contextDO = new ContextDO();
        contextDO.setId(contextId);
        contextDO.setCurrentTx(currentTxName);
        contextDO.setModifyTime(new Date());
        contextMapper.updateById(contextDO);
    }

    @Override
    public void savePreExecutedTx(long contextId, String preExecutedTxName) {
        ContextDO contextDO = new ContextDO();
        contextDO.setId(contextId);
        contextDO.setPreExecutedTx(preExecutedTxName);
        contextDO.setModifyTime(new Date());
        contextMapper.updateById(contextDO);
    }

    @Override
    public void savePreCompensatedTx(long contextId, String preCompensatedTxName) {
        ContextDO contextDO = new ContextDO();
        contextDO.setId(contextId);
        contextDO.setPreCompensatedTx(preCompensatedTxName);
        contextDO.setModifyTime(new Date());
        contextMapper.updateById(contextDO);
    }

    @Override
    public void incrementTriggerCount(long contextId) {
        contextMapper.incrementTriggerCount(contextId, new Date());
    }

    @Override
    public void saveNextTriggerTime(long contextId, Date nextTriggerTime) {
        ContextDO contextDO = new ContextDO();
        contextDO.setId(contextId);
        contextDO.setNextTriggerTime(nextTriggerTime);
        contextDO.setModifyTime(new Date());
        contextMapper.updateById(contextDO);
    }

    @Override
    public void saveContextInfo(long contextId, String key, Object info) {
        ContextInfoDO contextInfoDO = contextInfoMapper.selectByKey(contextId, key);
        if (contextInfoDO == null) {
            contextInfoDO = new ContextInfoDO();
            contextInfoDO.setContextId(contextId);
            contextInfoDO.setKey(key);
            String infoStr = serializeService.write(info);
            contextInfoDO.setInfo(infoStr);
            Date now = new Date();
            contextInfoDO.setCreateTime(now);
            contextInfoDO.setModifyTime(now);

            contextInfoMapper.insert(contextInfoDO);
        } else {

            String infoStr = serializeService.write(info);
            contextInfoDO.setKey(infoStr);
            contextInfoDO.setModifyTime(new Date());

            contextInfoMapper.updateById(contextInfoDO);
        }
    }

    @Override
    public <T> T loadContextInfo(long contextId, String key, Class<T> cls) {

        ContextInfoDO contextInfoDO = contextInfoMapper.selectByKey(contextId, key);
        if (contextInfoDO == null || contextInfoDO.getInfo() == null) {
            return null;
        }
        return serializeService.read(contextInfoDO.getInfo(), cls);
    }

    @Override
    public boolean lock(long id, String reqId, int timeoutSec) {

        try {
            ContextLockDO contextLockDO = contextLockMapper.select(id);
            if (contextLockDO == null) {
                contextLockDO = newLock(id, reqId, timeoutSec);
                contextLockMapper.insert(contextLockDO);
                return true;
            } else {
                Calendar c = Calendar.getInstance();
                boolean after = c.getTime().after(contextLockDO.getExpireTime());
                if (after) {
                    int delete = contextLockMapper.delete(id, contextLockDO.getReqId());
                    if (delete <= 0) {
                        return false;
                    } else {
                        contextLockDO = newLock(id, reqId, timeoutSec);
                        contextLockMapper.insert(contextLockDO);
                        return true;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    private ContextLockDO newLock(long id, String reqId, int timeoutSec) {
        ContextLockDO contextLockDO = new ContextLockDO();
        contextLockDO.setContextId(id);
        contextLockDO.setReqId(reqId);

        Calendar c = Calendar.getInstance();
        contextLockDO.setCreateTime(c.getTime());

        c.add(Calendar.SECOND, timeoutSec);
        contextLockDO.setExpireTime(c.getTime());

        return contextLockDO;
    }

    @Override
    public boolean unlock(long id, String reqId) {

        try {
            ContextLockDO contextLockDO = contextLockMapper.select(id);
            if (contextLockDO == null) {
                return false;
            } else {
                if (contextLockDO.getReqId().equals(reqId)) {
                    int delete = contextLockMapper.delete(id, reqId);
                    return delete > 0;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warn("操作数据库释放锁失败ContextId:{},ReqId:{}", new Object[] { id, reqId }, e);
            return false;
        }
    }

    @Override
    public List<Long> findNeedRetryContextList(int limit) {
        return contextMapper.findNeedRetryContextList(new Date(), limit);
    }

    private ContextDO sagaContextToContextDO(SagaContextBase base) {
        ContextDO contextDO = new ContextDO();
        try {
            BeanUtils.copyProperties(contextDO, base);
        } catch (Exception e) {
            throw new SagaException("SagaContextBase 转化为 ContextDO 失败, base: " + JSON.toJSONString(base));
        }
        return contextDO;
    }

    private SagaContextBase contextDOToSagaContext(ContextDO contextDO) {

        SagaContextBase base = new SagaContextBase();
        try {
            BeanUtils.copyProperties(base, contextDO);
        } catch (Exception e) {
            throw new SagaException("ContextDO 转化为 SagaContextBase 失败, contextDO: " + JSON.toJSONString(contextDO));
        }

        return base;
    }

}
