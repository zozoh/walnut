package com.site0.walnut.ext.media.edi.loader;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_DOC;
import com.site0.walnut.ext.media.edi.reply.EdiReplyCLNTDUP;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;

public class CLNTDUPLoader implements EdiMsgLoader<EdiReplyCLNTDUP> {

    @Override
    public EdiReplyCLNTDUP load(EdiMessage msg) {
        EdiReplyCLNTDUP re = new EdiReplyCLNTDUP();
        EdiSegmentFinder finder = msg.getFinder();
        EdiSegment seg;
        ICS_DOC t;

        // 如果能找到 ABN 或者 CCID 就算成功
        seg = finder.next("DOC", "^(ABN|CCI)$");
        if (null != seg) {
            t = new ICS_DOC(seg);
            re.setType(t.getNameCode());
            re.setCode(t.getDocName());
        }

        // 确定参考 ID
        finder.reset();
        seg = finder.next("RFF", "ABO");
        if (null != seg) {
            NutMap rff = new NutMap();
            seg.fillBean(rff, null, "type,rid");
            if (rff.is("type", "ABO")) {
                String rid = rff.getString("rid");
                if (null != rid) {
                    re.setReferId(rid);
                }
            }
        }

        return re;
    }

    @Override
    public Class<EdiReplyCLNTDUP> getResultType() {
        return EdiReplyCLNTDUP.class;
    }

}
