package com.site0.walnut.ext.media.edi.bean;

import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;

public class EdiErrSum {

    private boolean success;

    private int errCount;

    private EdiReplyError[] errs;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getErrCount() {
        return errCount;
    }

    public void setErrCount(int errCount) {
        this.errCount = errCount;
    }

    public EdiReplyError[] getErrs() {
        return errs;
    }

    public void setErrs(EdiReplyError[] errs) {
        this.errs = errs;
    }
}
