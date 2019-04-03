package org.mltds.sargeras.api.model;

import java.util.Date;

import org.mltds.sargeras.api.SagaTxStatus;

/**
 * @author sunyi.
 */
public class SagaTxRecord {

    private Long id;
    private Long recordId;
    private String cls;
    private String method;
    private String compensateMethod;
    private String parameterTypes;
    private String parameterNames;
    private SagaTxStatus status;
    private Date createTime;
    private Date modifyTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCompensateMethod() {
        return compensateMethod;
    }

    public void setCompensateMethod(String compensateMethod) {
        this.compensateMethod = compensateMethod;
    }

    public String getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public SagaTxStatus getStatus() {
        return status;
    }

    public void setStatus(SagaTxStatus status) {
        this.status = status;
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
