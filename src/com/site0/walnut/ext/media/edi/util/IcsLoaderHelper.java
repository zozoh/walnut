package com.site0.walnut.ext.media.edi.util;

import com.site0.walnut.ext.media.edi.bean.EdiErrSum;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.List;

public class IcsLoaderHelper {


    public static void fillResFuncCode(IcsCommonReply re, EdiSegmentFinder finder) {
        NutMap rff = new NutMap();
        finder.reset();
        EdiSegment seg = finder.next("BGM");
        seg.fillBean(rff, null, null, null, "funcCode");
        re.setFuncCode(rff.getInt("funcCode"));
    }

    /**
     * 解析 ERP-ERC-FTX 报文组, 收集错误信息
     * ERP+1'
     * ERC+ADVICE:80:95'
     * ERC+MS5202:6:95'
     * FTX+AAO+++THIS TRANSACTION WAS ACCEPTED WITH ERRORS AND/OR WARNINGS'
     * ERP+1'
     * ERC+ADVICE:80:95'
     * ERC+CL0378:6:95'
     * FTX+AAO+++CCID =AAA3437644L CREATED SUCCESSFULLY'
     */
    public static EdiReplyError[] parseERPErrs(EdiSegmentFinder finder) {
        finder.reset();
        boolean erpFound = finder.moveTo(true, "ERP");
        if (erpFound) {
            // 收集全部错误
            List<EdiReplyError> errList = new ArrayList<>();
            while (!finder.isEnd()) {
                List<EdiSegment> errs = finder.findContinueSegments("ERP", "^(ERC|FTX)$", "^(ERP|CNT|UNT)$");
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
            return errs;
        }
        return null;
    }

    /**
     * 计算错误信息条目的数量
     */
    public static int errCount(EdiReplyError[] errs) {
        int errCount = 0;
        for (EdiReplyError item : errs) {
            if ("ERROR".equalsIgnoreCase(item.getType())) {
                errCount++;
            }
        }
        return errCount;
    }


    public static EdiErrSum collectEdiErrSum(EdiSegmentFinder finder) {
        EdiErrSum re = new EdiErrSum();
        NutMap rff = new NutMap();

        // 1. 解析 SG4: ERP-ERC-FTX 报文组
        EdiReplyError[] errors = IcsLoaderHelper.parseERPErrs(finder);
        re.setErrs(errors);

        // 2. 解析 cnt 报文中的错误数量
        int cntErrNum = -1;
        List<EdiSegment> segmentList = finder.nextAll(true, "CNT");
        if (segmentList != null && segmentList.size() > 0) {
            for (EdiSegment item : segmentList) {
                rff.clear();
                item.fillBean(rff, null, "typeCode,errNum,");
                if (rff.is("typeCode", "55")) {
                    cntErrNum = rff.getInt("errNum", 0);
                    break;
                }
            }
        }
        if (cntErrNum > 0) {
            re.setSuccess(false);
        } else if (cntErrNum == 0) {
            re.setSuccess(true);
        } else if (cntErrNum == -1) {
            // 若未返回 errNum 报文，则根据是否有 error/warn 数据来判断
            boolean msgSuccess = true;
            if (errors != null && errors.length > 0) {
                for (EdiReplyError item : errors) {
                    String lowerType = item.getType() == null ? "" : item.getType().toLowerCase();
                    if (lowerType.contains("error") || lowerType.contains("warn")) {
                        msgSuccess = false;
                        break;
                    }
                }
            }
            re.setSuccess(msgSuccess);
        }

        // 3. 填充错误数量
        if (cntErrNum >= 0) {
            re.setErrCount(cntErrNum);
        } else {
            re.setErrCount(IcsLoaderHelper.errCount(errors));
        }

        // 4. 处理特殊情况
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

        return re;
    }

}
