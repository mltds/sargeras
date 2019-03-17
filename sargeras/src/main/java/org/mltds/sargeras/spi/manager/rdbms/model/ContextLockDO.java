package org.mltds.sargeras.spi.manager.rdbms.model;

import java.util.Date;

/**
 * @author sunyi.
 */
public class ContextLockDO {

    private Long id;
    private Long contextId;
    private String triggerId;
    private Date createTime;
    private Date expireTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }

    public String gettriggerId() {
        return triggerId;
    }

    public void settriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }
}
