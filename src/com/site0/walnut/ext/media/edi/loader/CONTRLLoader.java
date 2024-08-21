package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_UCD;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_UCI;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_UCM;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_UCS;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyCONTRL;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.List;

public class CONTRLLoader implements EdiMsgLoader<EdiReplyCONTRL> {

    @Override
    public EdiReplyCONTRL load(EdiMessage msg) {
        EdiReplyCONTRL re = new EdiReplyCONTRL();
        EdiSegmentFinder finder = msg.getFinder();

        // 解析 UCI 报文行
        // EdiSegment UCI = msg.findSegment("UCI");
        EdiSegment UCI = finder.next("UCI");
        if (null == UCI) {
            return null;
        }
        ICS_UCI uci = new ICS_UCI(UCI);
        re.setUci(uci);

        // 解析 UCM 报文行及附属信息
        List<ICS_UCM> msgs = new ArrayList<>();
        EdiSegment _seg;
        // 获取 UCM
        _seg = finder.tryNext("UCM");
        while (_seg != null) {
            ICS_UCM ics_ucm = new ICS_UCM(_seg);
            msgs.add(ics_ucm);
            // 循环 UCM 的下一层 UCS
            _seg = finder.tryNext("UCS");
            while (_seg != null) {
                ICS_UCS segErr = new ICS_UCS(_seg);
                ics_ucm.getSegsErr().add(segErr);

                // 循环 UCS 的下一层 UCD
                _seg = finder.tryNext("UCD");
                while (_seg != null) {
                    ICS_UCD errItem = new ICS_UCD(_seg);
                    segErr.getErrItems().add(errItem);
                    _seg = finder.tryNext("UCD");
                }
                _seg = finder.tryNext("UCS");
            }
            _seg = finder.tryNext("UCM");
        }
        re.setMsgs(msgs);
        return re;
    }

    @Override
    public Class<EdiReplyCONTRL> getResultType() {
        return EdiReplyCONTRL.class;
    }

}
