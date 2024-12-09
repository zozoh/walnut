package com.site0.walnut.ext.media.edi.msg.reply.cargorpt;

import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

import java.util.Map;

public class IcsReplySEACRR extends IcsCommonReply {

    /**
     * 如果错误，这里是具体的错误信息
     */
    private EdiReplyError[] errs;

    /**
     * 错误条目的数量
     */
    private int errCount;

    /**
     * (1) 存放 NAD 相关信息;
     * (2) 存放 RFF 相关信息;
     * (3) 存放 DTM 相关信息。
     */
    private Map<String, Object> extraInfo;

    public IcsReplySEACRR() {
        super("SEACRR");
    }


    public EdiReplyError[] getErrs() {
        return errs;
    }

    public void setErrs(EdiReplyError[] errs) {
        this.errs = errs;
    }

    public int getErrCount() {
        return errCount;
    }

    public void setErrCount(int errCount) {
        this.errCount = errCount;
    }

    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Map<String, Object> extraInfo) {
        this.extraInfo = extraInfo;
    }
}
