package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.ubm.IcsReplyUbmErr;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.apache.commons.lang3.StringUtils;
import org.nutz.lang.util.NutMap;

import java.util.LinkedHashMap;
import java.util.List;

public class UBMErrLoader implements EdiMsgLoader<IcsReplyUbmErr> {

    @Override
    public Class<IcsReplyUbmErr> getResultType() {
        return IcsReplyUbmErr.class;
    }

    @Override
    public IcsReplyUbmErr load(EdiMessage msg) {
        IcsReplyUbmErr re = new IcsReplyUbmErr();
        // BGM 报文中的 FUNCTION CODE
        re.setFuncCode(11);

        EdiSegmentFinder finder = msg.getFinder();
        NutMap rff = new NutMap();
        List<EdiSegment> segmentList;


        /**
         * 定位到 BGM 报文行，解析 Version 和 FuncCode
         * BGM+961:::UBMREQE+1812 F23I 46AE:001+11'
         */
        IcsLoaderHelper.fillVerAndFuncCode(re, finder);

        // 解析 FTX 报文行 (根据目前的样例，这个报文行是不会出现在 UBMREQE 中的)
        segmentList = finder.nextAll(true, "FTX");
        LinkedHashMap<String, String> stMap = new LinkedHashMap();
        for (EdiSegment item : segmentList) {
            rff.clear();
            // example: FTX+AHN+++{StatusType}:{StatusDescription}'
            item.fillBean(rff, null, "subjectCode", null, null, "statusType,statusDesc");
            if (rff.is("subjectCode", "AHN")) {
                stMap.put(rff.getString("statusType"), rff.getString("statusDesc"));
            }
        }
        if (stMap.size() > 0) {
            re.setStInfo(stMap);
        }

        // 定位到 NAD 报文行
        finder.moveTo(true, "RFF");

        /*
         * 解析 RFF 报文行
         * RFF+ACW:UBMREQ'
         * RFF+AFM:9'
         * RFF+ABO:U00000337/PRD1::001'
         * */
        segmentList = finder.nextAll(true, "RFF");
        for (EdiSegment item : segmentList) {
            rff.clear();
            item.fillBean(rff, null, "refCode,refVal,,refVer");
            String refCode = rff.getString("refCode");
            if (rff.is("funcCode", "ACW")) {
                re.setInDocName(rff.getString("refVal"));
            } else if (rff.is("funcCode", "AFM")) {
                re.setInMsgFuncCode(rff.getString("refVal"));
            } else if (rff.is("funcCode", "ABO")) {
                String referId = rff.getString("refVal");
                if (referId != null) {
                    re.setRefId(referId);
                    re.setRefIdInLower(referId.toLowerCase());
                    re.setRefVer(rff.getInt("refVer"));
                }
            }
        }

        // 解析 DTM 报文行: DTM+310:20240612115040:204'
        segmentList = finder.nextAll(true, "DTM");
        for (EdiSegment item : segmentList) {
            rff.clear();
            item.fillBean(rff, null, "funcCode,dateTime");
            re.setMsgRcvTime(rff.getString("dateTime"));
        }

        // 解析 SG4: ERP-ERC-FTX 报文组
        EdiReplyError[] errors = IcsLoaderHelper.parseERPErrs(finder);
        re.setErrs(errors);
        re.setErrCount(IcsLoaderHelper.errCount(errors));

        // 判断本次 UBM 是否成功

        // todo  判断是否有错误的例外情况的时候，参考 cargoreport 的判断方式
//        boolean hasRejectedContent = false;
//        boolean hasAcceptedContent = false;
//        String rejectedContent = "THIS TRANSACTION WAS REJECTED";
//        String acceptedContent = "THIS TRANSACTION WAS ACCEPTED";

        return re;
    }
}
