package org.nutz.walnut.ext.media.edi.loader;

import org.nutz.walnut.ext.media.edi.bean.EdiMessage;
import org.nutz.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.walnut.ext.media.edi.bean.segment.ICS_UCI;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyIC;

public class ICLoader implements EdiMsgLoader<EdiReplyIC> {

    @Override
    public EdiReplyIC trans(EdiMessage en) {
        EdiReplyIC re = new EdiReplyIC();
        EdiSegment UCI = en.findSegment("UCI");
        if (null == UCI) {
            return null;
        }
        ICS_UCI uci = new ICS_UCI(UCI);
        re.setUCI(uci);
        return re;
    }

    @Override
    public Class<EdiReplyIC> getResultType() {
        return EdiReplyIC.class;
    }

}
