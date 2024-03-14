package com.site0.walnut.ext.media.edi.reply;

/**
 * CLREG的返回
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class EdiReplyCLREGR extends EdiReplyCL {

    public EdiReplyCLREGR() {
        super("CLREGR");
    }

    private boolean success;

    /**
     * 如果错误，这里是具体的错误信息
     */
    private EdiReplyError[] errors;

    public boolean isFailed() {
        return !this.success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public EdiReplyError[] getErrors() {
        return errors;
    }

    public void setErrors(EdiReplyError[] errors) {
        this.errors = errors;
    }

}
