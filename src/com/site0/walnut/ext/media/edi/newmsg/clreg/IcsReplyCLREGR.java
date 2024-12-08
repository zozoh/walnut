package com.site0.walnut.ext.media.edi.newmsg.clreg;

import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;

public class IcsReplyCLREGR extends IcsCommonReply {

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

    /**
     * 如果错误，这里是具体的错误信息
     */
    private EdiReplyError[] errs;

    private int errCount;

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
}
