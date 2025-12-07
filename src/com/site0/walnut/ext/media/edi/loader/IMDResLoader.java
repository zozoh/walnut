package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.imd.IcsReplyImdRes;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.util.NutMap;

import java.util.List;

public class IMDResLoader implements EdiMsgLoader<IcsReplyImdRes> {


    @Override
    public Class<IcsReplyImdRes> getResultType() {
        return IcsReplyImdRes.class;
    }

    @Override
    public IcsReplyImdRes load(EdiMessage msg) {
        IcsReplyImdRes re = new IcsReplyImdRes();
        // 本次解析结果版本号，本版本为 0
        re.setRstVer(0);
        // BGM 报文中的 FUNCTION CODE, 此报文固定为 11: Response
        re.setFuncCode(11);

        EdiSegmentFinder finder = msg.getFinder();
        NutMap rff = new NutMap();
        List<EdiSegment> segs;

        // 定位到 BGM 报文行，解析 FuncCode
        IcsLoaderHelper.fillResFuncCode(re, finder);

        // 定位到 DTM 报文行
        boolean find = finder.moveToUtil("DTM", true, "NAD");
        if (find) {
            segs = finder.nextAll(true, "DTM");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "funcCode,dateTime,formatCode");
                if (rff.is("funcCode", "9") && rff.is("formatCode", "102")) {
                    re.setDutyDate(rff.getString("dateTime"));
                }
            }
        }

        // 定位到 DTM 报文行
        find = finder.moveToUtil("FTX", true, "NAD");
        if (find) {
            segs = finder.nextAll(true, "FTX");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "subjectCode", null, null, "statusType,statusDesc");
                // AHN: Status details
                if (rff.is("subjectCode", "AHN")) {
                    String stType = rff.getString("statusType");
                    String stDesc = rff.getString("statusDesc");
                    stType = stType != null ? stType.trim() : null;
                    stDesc = stDesc != null ? stDesc.trim() : null;
                    re.getImdStatus().put(stType, stDesc);
                }
            }
        }

        // 	定位到 GIS 报文行 (General Indicator C10)
        find = finder.moveToUtil("GIS", true, "NAD");
        if (find) {
            segs = finder.nextAll(true, "GIS");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "indDescCode,indCode,agencyCode");
                // AHN: Status details
                if (rff.is("indDescCode", "95")) {
                    if (rff.is("indCode", "117")) {
                        if (rff.is("indDescCode", "Y")) {
                            re.setPreLodge(true);
                        } else if (rff.is("indDescCode", "N")) {
                            re.setPreLodge(false);
                        }
                    }
                }

            }
        }


        return null;
    }
}
