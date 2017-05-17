package org.nutz.walnut.ext.payment;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 支付接口的返回
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnPay3xRe {

    private String payObjId;

    private WnPay3xStatus status;

    private Set<String> changedKeys;

    private WnPay3xDataType dataType;

    private Object data;

    public String getPayObjId() {
        return payObjId;
    }

    public void setPayObjId(String payObjId) {
        this.payObjId = payObjId;
    }

    public boolean isStatusOk() {
        return WnPay3xStatus.OK == this.status;
    }

    public boolean isStatusFail() {
        return WnPay3xStatus.FAIL == this.status;
    }

    public boolean isStatusWait() {
        return WnPay3xStatus.WAIT == this.status;
    }

    public boolean isDone() {
        return WnPay3xStatus.FAIL == this.status || WnPay3xStatus.OK == this.status;
    }

    public WnPay3xStatus getStatus() {
        return status;
    }

    public void setStatus(WnPay3xStatus status) {
        this.status = status;
    }

    public boolean isDataTypeLink() {
        return WnPay3xDataType.LINK == this.dataType;
    }

    public boolean isDataTypeQrcode() {
        return WnPay3xDataType.QRCODE == this.dataType;
    }

    public boolean isDataTypeJson() {
        return WnPay3xDataType.JSON == this.dataType;
    }

    public WnPay3xDataType getDataType() {
        return dataType;
    }

    public void setDataType(WnPay3xDataType type) {
        this.dataType = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getDataAsString() {
        if (null == this.data)
            return null;
        if (this.data instanceof String)
            return (String) this.data;
        return this.data.toString();
    }

    public boolean hasChangedKeys() {
        return null != this.changedKeys && this.changedKeys.size() > 0;
    }

    public Collection<String> getChangedKeys() {
        return changedKeys;
    }

    public void setChangedKeys(List<String> changedKeys) {
        if (null == this.changedKeys) {
            this.changedKeys = new HashSet<>();
        }
        this.changedKeys.addAll(changedKeys);
    }

    public void addChangeKeys(String... keys) {
        if (null == this.changedKeys) {
            this.changedKeys = new HashSet<>();
        }
        for (String key : keys)
            this.changedKeys.add(key);
    }

}
