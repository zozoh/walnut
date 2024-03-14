package com.site0.walnut.ext.media.edi.reply;

public abstract class EdiReplyCL extends EdiReplyObj {

    public EdiReplyCL(String replyType) {
        super(replyType);
    }

    /**
     * 消息发送记录的 ID ，一般就是发件箱发送对象的 id。
     */
    private String referId;

    /**
     * referId 的小写形式
     */
    private String referIdInLower;

    /**
     * 注册成功，会返回注册码，这里说明注册码的类型
     * <ul>
     * <li><code>ABN</code>
     * <li><code>CCI</code>
     * </ul>
     */
    private String type;

    /**
     * ABN 或者 CCID 的具体代码
     */
    private String code;

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

}
