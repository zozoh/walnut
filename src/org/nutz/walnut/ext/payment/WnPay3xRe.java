package org.nutz.walnut.ext.payment;

import java.util.List;

/**
 * 支付接口的返回
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnPay3xRe {

    private WnPay3xStatus status;

    private List<String> changedKeys;

    private WnPay3xDataType dataType;

    private Object data;

    public boolean isStatusOk() {
        return WnPay3xStatus.OK == this.status;
    }

    public boolean isStatusFail() {
        return WnPay3xStatus.FAIL == this.status;
    }

    public boolean isStatusWait() {
        return WnPay3xStatus.WAIT == this.status;
    }

    public WnPay3xStatus getStatus() {
        return status;
    }

    public WnPay3xRe setStatus(WnPay3xStatus status) {
        this.status = status;
        return this;
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

    public WnPay3xRe setDataType(WnPay3xDataType type) {
        this.dataType = type;
        return this;
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

    public List<String> getChangedKeys() {
        return changedKeys;
    }

    public void setChangedKeys(List<String> changedKeys) {
        this.changedKeys = changedKeys;
    }

}
