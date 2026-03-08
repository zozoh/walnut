package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.msg.reply.atd.IcsReplyAtdRes;

public class ATDResLoader implements EdiMsgLoader<IcsReplyAtdRes>{
    @Override
    public Class<IcsReplyAtdRes> getResultType() {
        return IcsReplyAtdRes.class;
    }

    @Override
    public IcsReplyAtdRes load(EdiMessage msg) {
        return null;
    }
}
