package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.contrl.CntrEleErr;
import com.site0.walnut.ext.media.edi.msg.reply.contrl.CntrIcRes;
import com.site0.walnut.ext.media.edi.msg.reply.contrl.CntrMsgRes;
import com.site0.walnut.ext.media.edi.msg.reply.contrl.CntrSegErr;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyCONTRL;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;

import java.util.ArrayList;
import java.util.List;

public class CONTRLLoader implements EdiMsgLoader<EdiReplyCONTRL> {

    @Override
    public EdiReplyCONTRL load(EdiMessage msg) {
        EdiReplyCONTRL re = new EdiReplyCONTRL();
        EdiSegmentFinder finder = msg.getFinder();

        /**
         * 解析 UCI 报文行:
         * 1. 模板: `UCI+{InterchangeControlReferenceNumber}+{InbCreator}::{InbOwner}+{InbRecipientId}+{ActionCode}+{ErrorCode}+{SegmentTag}+{DateElementPos}:{ComponentDataPos}'`
         * 2. 样例: `UCI+23031014150005+AAR399A::AAR399A+AAA336C+7'`
         * */
        EdiSegment UCI = finder.next("UCI");
        if (null == UCI) {
            return null;
        }
        CntrIcRes uci = new CntrIcRes(UCI);
        re.setIc(uci);

        // 解析 UCM 报文行及附属信息
        List<CntrMsgRes> msgs = new ArrayList<>();
        EdiSegment _seg;
        // 获取 UCM
        _seg = finder.tryNext("UCM");
        while (_seg != null) {
            CntrMsgRes ics_ucm = new CntrMsgRes(_seg);
            msgs.add(ics_ucm);
            // 循环 UCM 的下一层 UCS
            _seg = finder.tryNext("UCS");
            while (_seg != null) {
                CntrSegErr segErr = new CntrSegErr(_seg);
                ics_ucm.getSegsErr().add(segErr);

                // 循环 UCS 的下一层 UCD
                _seg = finder.tryNext("UCD");
                while (_seg != null) {
                    CntrEleErr errItem = new CntrEleErr(_seg);
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
