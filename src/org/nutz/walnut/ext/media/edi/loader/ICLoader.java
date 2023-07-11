package org.nutz.walnut.ext.media.edi.loader;

import org.nutz.walnut.ext.media.edi.bean.EdiMessage;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyIC;

public class ICLoader implements EdiMsgLoader<EdiReplyIC> {

    @Override
    public EdiReplyIC trans(EdiMessage en) {
        return null;
    }

    @Override
    public Class<EdiReplyIC> getResultType() {
        return EdiReplyIC.class;
    }

}
