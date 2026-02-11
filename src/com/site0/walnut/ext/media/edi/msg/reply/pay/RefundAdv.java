package com.site0.walnut.ext.media.edi.msg.reply.pay;

import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;

public class RefundAdv extends IcsCommonReply {

    public RefundAdv(String msgType) {
        super("REFACC");
    }


}
