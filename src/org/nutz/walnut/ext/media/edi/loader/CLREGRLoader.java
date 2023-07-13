package org.nutz.walnut.ext.media.edi.loader;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.media.edi.bean.EdiMessage;
import org.nutz.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.walnut.ext.media.edi.bean.segment.ICS_FTX;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyCLREGR;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyError;
import org.nutz.walnut.ext.media.edi.util.EdiSegmentFinder;

public class CLREGRLoader implements EdiMsgLoader<EdiReplyCLREGR> {

    @Override
    public EdiReplyCLREGR trans(EdiMessage msg) {
        EdiReplyCLREGR re = new EdiReplyCLREGR();
        EdiSegmentFinder finder = msg.getFinder();
        EdiSegment seg;
        ICS_FTX t;

        // 如果能找到 ABN 或者 CCID 就算成功
        seg = finder.next("FTX", "^(ABN|CCID)$");
        if (null != seg) {
            t = new ICS_FTX(seg);
            re.setSuccess(true);
            re.setType(t.getSubjectCode());
            re.setCode(t.getReference());
        }
        // 那就是失败
        else {
            re.setSuccess(false);
        }

        // 首先确定参考 ID
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

        // 收集错误信息
        if (re.isFailed()) {
            // 定位一个错误信息
            seg = finder.next("ERP");
            // 收集全部错误
            List<EdiReplyError> errList = new ArrayList<>();
            while (null != seg) {
                List<EdiSegment> errs = finder.nextUntil(false, "ERP");
                // TODO 必然是三条报文，需要加载为 EdiReplyError

            }
            // 记入错误
            EdiReplyError[] errs = new EdiReplyError[errList.size()];
            errList.toArray(errs);
            re.setErrors(errs);
        }

        return re;
    }

    @Override
    public Class<EdiReplyCLREGR> getResultType() {
        return EdiReplyCLREGR.class;
    }

}
