package org.mltds.sargeras.repository.rdbms.model;

import java.util.Date;

import org.mltds.sargeras.api.SagaStatus;

/**
 * @author sunyi
 */
public class ContextDO {

    private Long id;
    private String appName;
    private String bizName;
    private String bizId;
    private SagaStatus status;
    private String currentTx;
    private String preExecutedTx;
    private String preCompensatedTx;
    private Date createTime;
    private Date modifyTime;

    public ContextDO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
        this.status = status;
    }

    public String getPreExecutedTx() {
        return preExecutedTx;
    }

    public void setPreExecutedTx(String preExecutedTx) {
        this.preExecutedTx = preExecutedTx;
    }

    public String getCurrentTx() {
        return currentTx;
    }

    public void setCurrentTx(String currentTx) {
        this.currentTx = currentTx;
    }

    public String getPreCompensatedTx() {
        return preCompensatedTx;
    }

    public void setPreCompensatedTx(String preCompensatedTx) {
        this.preCompensatedTx = preCompensatedTx;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

}