package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
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
        IcsLoaderHelper.fillVerAndFuncCode(re, finder);

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

        // 解析 SG4: ERP-ERC-FTX 报文组
        EdiReplyError[] errors = IcsLoaderHelper.parseERPErrs(finder);
        re.setErrs(errors);
        re.setErrCount(IcsLoaderHelper.errCount(errors));

        // 判断本次 CargoReport 是否成功
        int errNum = -1;
        segs = finder.nextAllUtilNoMatch(false, "CNT");
        if (segs != null && segs.size() > 0) {
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "typeCode,errNum,");
                if (rff.is("typeCode", "55")) {
                    errNum = rff.getInt("errNum", 0);
                    break;
                }
            }
        }
        if (errNum > 0) {
            re.setSuccess(false);
        } else if (errNum == 0) {
            re.setSuccess(true);
        } else if (errNum == -1) {
            // 若未返回 errNum 报文，则根据是否有 error/warn 数据来判断
            boolean msgSuccess = true;
            EdiReplyError[] errArr = re.getErrs();
            if (errArr != null && errArr.length > 0) {
                for (EdiReplyError item : errArr) {
                    String lowerType = item.getType() == null ? "" : item.getType().toLowerCase();
                    if (lowerType.contains("error") || lowerType.contains("warn")) {
                        msgSuccess = false;
                        break;
                    }
                }
            }
            re.setSuccess(msgSuccess);
        }
        // 如果报文回复了错误的数量，那么覆盖计算出来的错误数量
        if (errNum >= 0) {
            re.setErrCount(errNum);
        }

        // (1)有时, 虽然 ErrCount = 0, 但是错误报文的信息包含 "THIS TRANSACTION WAS REJECTED" 文本，这种情况属于报文被拒绝(Success = false)
        // (2)有时, 虽然 ErrCount > 0, 但是错误报文的信息包含 "THIS TRANSACTION WAS ACCEPTED" 文本，这种情况属于报文被接受(Success = true)
        boolean hasRejectedContent = false;
        boolean hasAcceptedContent = false;
        String rejectedContent = "THIS TRANSACTION WAS REJECTED";
        String acceptedContent = "THIS TRANSACTION WAS ACCEPTED";
        for (EdiReplyError item : re.getErrs()) {
            String content = item.getContent() == null ? "" : item.getContent();
            if (content.contains(acceptedContent)) {
                hasAcceptedContent = true;
            } else if (content.contains(rejectedContent)) {
                hasRejectedContent = true;
            }
            if (hasAcceptedContent || hasRejectedContent) {
                break;
            }
        }
        if (re.isSuccess() && hasRejectedContent) {
            re.setSuccess(false);
        }
        if (!re.isSuccess() && hasAcceptedContent) {
            re.setSuccess(true);
        }

        // 返回解析结果
        return re;
    }
}
