package org.nutz.walnut.ext.media.edi.reply;

/**
 * CLREG的返回
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class EdiReplyCLREGR {

    private int stateCode;

    private String messageId;

    private EdiReplyError[] errors;

    public boolean isOk() {
        return 4 == this.stateCode;
    }

    public boolean isFail() {
        return 7 == this.stateCode;
    }

    public int getStateCode() {
        return stateCode;
    }

    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public EdiReplyError[] getErrors() {
        return errors;
    }

    public void setErrors(EdiReplyError[] errors) {
        this.errors = errors;
    }

}
