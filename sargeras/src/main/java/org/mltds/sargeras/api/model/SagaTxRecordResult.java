package org.mltds.sargeras.api.model;

import java.util.Date;

/**
 * @author sunyi.
 */
public class SagaTxRecordResult {

    private Long id;
    private Long recordId;
    private Long txRecordId;
    private String cls;
    private byte[] result;
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

    public Long getTxRecordId() {
        return txRecordId;
    }

    public void setTxRecordId(Long txRecordId) {
        this.txRecordId = txRecordId;
    }

    public String getCls() {
        return cls;
    }

    public void setCls(String cls) {
        this.cls = cls;
    }

    public byte[] getResult() {
        return result;
    }

    public void setResult(byte[] result) {
        this.result = result;
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
