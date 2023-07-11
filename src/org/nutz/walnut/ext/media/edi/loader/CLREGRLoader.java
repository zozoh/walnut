package org.nutz.walnut.ext.media.edi.loader;

import org.nutz.walnut.ext.media.edi.bean.EdiMessage;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyCLREGR;

public class CLREGRLoader implements EdiMsgLoader<EdiReplyCLREGR> {

    @Override
    public EdiReplyCLREGR trans(EdiMessage en) {
        return null;
    }

    @Override
    public Class<EdiReplyCLREGR> getResultType() {
        return EdiReplyCLREGR.class;
    }

}
