package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiErrSum;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.cargorpt.IcsReplySEACRR;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.util.NutMap;

import java.util.HashMap;
import java.util.List;

public class SEACRRLoader implements EdiMsgLoader<IcsReplySEACRR> {

    protected String ediType;

    public SEACRRLoader() {
        this("SEACRR");
    }

    protected SEACRRLoader(String ediType) {
        this.ediType = ediType;
    }

    @Override
    public Class<IcsReplySEACRR> getResultType() {
        return IcsReplySEACRR.class;
    }

    @Override
    public IcsReplySEACRR load(EdiMessage msg) {
        IcsReplySEACRR re = new IcsReplySEACRR(this.ediType);
        re.setExtraInfo(new HashMap<>());

        EdiSegmentFinder finder = msg.getFinder();
        NutMap rff = new NutMap();
        List<EdiSegment> segs;
        // EdiSegment seg;

        /**
         * 定位到 BGM 报文行，解析 Version 和 FuncCode
         * BGM+961:::SEACRR+193B JJ26 651E:001+11'
         */
        IcsLoaderHelper.fillResFuncCode(re, finder);

        // 解析 FTX 报文行
        segs = finder.nextAllUtilNoMatch(false, "FTX");
        for (EdiSegment item : segs) {
            rff.clear();
            item.fillBean(rff, null, "subjectCode", null, null, "statusType,statusDesc");
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
        segs = finder.nextAllUtilNoMatch(false, "RFF");
        if (segs != null && segs.size() > 0) {
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "refType,refId,,refVer");
                if (rff.is("refType", "ACW")) {
                    re.getExtraInfo().put("msgType", rff.getString("refId"));
                } else if (rff.is("refType", "AFM")) {
                    re.getExtraInfo().put("msgFuncType", rff.getString("refId"));
                } else if (rff.is("refType", "ABO")) {
                    String referenceId = rff.getString("refId");
                    if (referenceId != null) {
                        re.setRefId(referenceId);
                        re.setRefIdInLower(referenceId.toLowerCase());
                        re.setRefVer(rff.getInt("refVer"));
                    }
                }
            }
        }
        segs = finder.nextAllUtilNoMatch(false, "DTM");
        if (segs != null && segs.size() > 0) {
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "funcCode,msgRcvTime,");
                if (rff.is("funcCode", "310")) {
                    re.getExtraInfo().put("msgRcvTime", rff.getString("msgRcvTime"));
                    break;
                }
            }
        }

        // 解析错误信息:  解析 SG4: ERP-ERC-FTX 报文组 , 以及 CNT+55:${CountNum}' 报文行
        EdiErrSum ediErrSum = IcsLoaderHelper.collectEdiErrSum(finder);
        re.setErrs(ediErrSum.getErrs());
        re.setErrCount(ediErrSum.getErrCount());
        re.setSuccess(ediErrSum.isSuccess());

        // 返回解析结果
        return re;
    }
}
