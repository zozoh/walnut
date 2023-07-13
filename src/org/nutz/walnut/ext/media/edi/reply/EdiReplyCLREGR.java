package org.nutz.walnut.ext.media.edi.reply;

/**
 * CLREG的返回
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class EdiReplyCLREGR {

    /**
     * 消息发送记录的 ID ，一般就是发件箱发送对象的 id。
     */
    private String referId;

    /**
     * referId 的小写形式
     */
    private String referIdInLower;

    private boolean success;

    /**
     * 注册成功，会返回注册码，这里说明注册码的类型
     * <ul>
     * <li><code>ABN</code>
     * <li><code>CCID</code>
     * </ul>
     */
    private String type;

    /**
     * ABN 或者 CCID 的具体代码
     */
    private String code;

    /**
     * 如果错误，这里是具体的错误信息
     */
    private EdiReplyError[] errors;

    public String getReferId() {
        return referId;
    }

    public void setReferId(String referId) {
        this.referId = referId;
        this.referIdInLower = null == referId ? null : referId.toLowerCase();
    }

    public String getReferIdInLower() {
        return referIdInLower;
    }

    public boolean isFailed() {
        return !this.success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public EdiReplyError[] getErrors() {
        return errors;
    }

    public void setErrors(EdiReplyError[] errors) {
        this.errors = errors;
    }

}
