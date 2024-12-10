package com.site0.walnut.ext.media.edi.util;

import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.IcsCommonReply;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.List;

public class IcsLoaderHelper {


    public static void fillVerAndFuncCode(IcsCommonReply re, EdiSegmentFinder finder) {
        NutMap rff = new NutMap();
        finder.reset();
        EdiSegment seg = finder.next("BGM");
        seg.fillBean(rff, null, null, ",verionNum", "funcCode");
        re.setRefVer(rff.getInt("verionNum", 0));
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

}
