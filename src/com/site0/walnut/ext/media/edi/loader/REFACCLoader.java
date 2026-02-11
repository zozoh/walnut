package com.site0.walnut.ext.media.edi.loader;


import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.pay.RefundAdv;

public class REFACCLoader implements EdiMsgLoader<RefundAdv>{

    @Override
    public Class<RefundAdv> getResultType() {
        return RefundAdv.class;
    }

    @Override
    public RefundAdv load(EdiMessage msg) {
        return null;
    }
}
