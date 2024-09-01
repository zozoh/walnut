package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.cargorpt.EdiReplyAIRCRR;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AIRCRRLoader implements EdiMsgLoader<EdiReplyAIRCRR> {
    @Override
    public Class<EdiReplyAIRCRR> getResultType() {
        return EdiReplyAIRCRR.class;
    }

    @Override
    public EdiReplyAIRCRR load(EdiMessage msg) {
        EdiReplyAIRCRR re = new EdiReplyAIRCRR();
        re.setExtraInfo(new HashMap<>());

        EdiSegmentFinder finder = msg.getFinder();
        NutMap rff = new NutMap();
        List<EdiSegment> segs;
        EdiSegment seg;

        // 定位到 BGM 报文行
        seg = finder.next("BGM");

        // 解析 FTX 报文行
        segs = finder.nextAllUtilNoMatch(false, "FTX");
        for (EdiSegment item : segs) {
            rff.clear();
            seg.fillBean(rff, null, "subjectCode", null, null, "statusType,statusDesc");
            String subjectCode = rff.getString("subjectCode");
            if (rff.is("subjectCode", "AHN")) {
                re.getExtraInfo().put("statusType", rff.getString("statusType"));
                re.getExtraInfo().put("statusDesc", rff.getString("statusDesc"));
            }
        }

        // 解析 NAD 报文行
        segs = finder.nextAllUtilNoMatch(false, "NAD");
        for (EdiSegment item : segs) {
            rff.clear();
            item.fillBean(rff, null, "funcCode", "icsSiteId");
            if (rff.is("funcCode", "MR")) {
                String icsSiteId = rff.getString("icsSiteId");
                re.getExtraInfo().put("icsSiteId", icsSiteId);
            }
        }

        // 解析 SG3: RFF-DTM 报文组
        List<EdiSegment> _segs = finder.nextAllUtilNoMatch(false, "RFF");
        if (_segs != null && _segs.size() > 0) {
            for (EdiSegment _seg : _segs) {
                rff.clear();
                _seg.fillBean(rff, null, "refType,refId,,refVer");
                if (rff.is("refType", "ACW")) {
                    re.getExtraInfo().put("msgType", rff.getString("refId"));
                } else if (rff.is("refType", "AFM")) {
                    re.getExtraInfo().put("msgFuncType", rff.getString("refId"));
                } else if (rff.is("refType", "ABO")) {
                    String referenceId = rff.getString("refId");
                    if (referenceId != null) {
                        re.setReferId(referenceId);
                        re.setReferIdInLower(referenceId.toLowerCase());
                        re.setRefVer(rff.getInt("refVer"));
                    }
                }
            }
        }


        // 解析 SG4: ERP-ERC-FTX 报文组
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

        // 判断本次 CargoReport 是否成功
        boolean msgSuccess = true;
        if (errList.size() > 0) {
            for (EdiReplyError item : errList) {
                String lowerType = item.getType() == null ? "" : item.getType().toLowerCase();
                if (lowerType.contains("error") || lowerType.contains("warn")) {
                    msgSuccess = false;
                    break;
                }
            }
        }
        re.setSuccess(msgSuccess);

        // 返回解析结果
        return re;
    }
}
