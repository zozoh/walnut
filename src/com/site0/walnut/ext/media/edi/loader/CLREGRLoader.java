package com.site0.walnut.ext.media.edi.loader;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.bean.segment.ICS_FTX;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyCLREGR;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;

public class CLREGRLoader implements EdiMsgLoader<EdiReplyCLREGR> {

    @Override
    public EdiReplyCLREGR load(EdiMessage msg) {
        EdiReplyCLREGR re = new EdiReplyCLREGR();
        EdiSegmentFinder finder = msg.getFinder();
        EdiSegment seg;
        ICS_FTX t;

        // 如果能找到 ABN 或者 CCID 就算成功
        seg = finder.next("FTX", "^(ABN|CCI)$");
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

        // 收集错误信息
        if (re.isFailed()) {
            // 定位一个错误信息
            seg = finder.next("ERP");
            // 收集全部错误
            List<EdiReplyError> errList = new ArrayList<>();
            while (!finder.isEnd()) {
                List<EdiSegment> errs = finder.nextUntil(false, "^(ERP|UNT|CNT)$");
                // 看来找不到错误了，那么退出循环
                if (errs.isEmpty() || !errs.get(0).is("ERC")) {
                    break;
                }
                // 必然是三条报文，需要加载为 EdiReplyError
                EdiReplyError err = new EdiReplyError(errs);
                errList.add(err);
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
