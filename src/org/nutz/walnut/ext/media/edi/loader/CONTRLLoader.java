package org.nutz.walnut.ext.media.edi.loader;

import org.nutz.walnut.ext.media.edi.bean.EdiMessage;
import org.nutz.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.walnut.ext.media.edi.bean.segment.ICS_UCI;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyCONTRL;

public class CONTRLLoader implements EdiMsgLoader<EdiReplyCONTRL> {

    @Override
    public EdiReplyCONTRL load(EdiMessage msg) {
        EdiReplyCONTRL re = new EdiReplyCONTRL();
        EdiSegment UCI = msg.findSegment("UCI");
        if (null == UCI) {
            return null;
        }
        ICS_UCI uci = new ICS_UCI(UCI);
        re.setUci(uci);
        return re;
    }

    @Override
    public Class<EdiReplyCONTRL> getResultType() {
        return EdiReplyCONTRL.class;
    }

}
