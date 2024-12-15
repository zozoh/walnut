package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.IcsReplyCARST;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.util.NutMap;

import java.util.List;

public class CARSTLoader implements EdiMsgLoader<IcsReplyCARST> {


    @Override
    public Class<IcsReplyCARST> getResultType() {
        return IcsReplyCARST.class;
    }

    @Override
    public IcsReplyCARST load(EdiMessage msg) {
        IcsReplyCARST re = new IcsReplyCARST();
        EdiSegmentFinder finder = msg.getFinder();
        NutMap rff = new NutMap();

        re.setSuccess(true);

        // 定位到 BGM 报文行
        IcsLoaderHelper.fillVerAndFuncCode(re, finder);

        // 解析 DTM 报文行(C-9)
        List<EdiSegment> segmentList = finder.nextAll(true, "DTM");
        for (EdiSegment item : segmentList) {
            rff.clear();
            item.fillBean(rff, null, "funcCode,dateTime");
            if (rff.is("funcCode", "9")) {
                re.getInfoMap().put("txnTime", rff.getString("dateTime"));
            } else if (rff.is("funcCode", "132")) {
                re.getInfoMap().put("etaDate", rff.getString("dateTime"));
            }
        }

        // 解析 FTX+AHN 报文行 (各类状态)
        segmentList = finder.nextAll(true, "FTX");
        for (EdiSegment item : segmentList) {
            rff.clear();
            item.fillBean(rff, null, "subjectCode", null, null, "stName,stVal");
            if (rff.is("subjectCode", "AHN")) {
                String stName = rff.getString("stName");
                if (stName != null) {
                    stName = stName.trim();
                }
                String stVal = rff.getString("stVal");
                if (stVal != null) {
                    stVal = stVal.trim();
                }

                // IMPEDIMENT DETAILS
                if (stName != null && stName.contains("IMPEDIMENT DETAILS")) {
                    re.getImpends().add(stVal);
                } else {
                    re.getStatusMap().put(stName, stVal);
                }
            }
        }

        // 解析 TDT	报文行 (C-9) : 运输方式及信息
        segmentList = finder.nextAll(true, "TDT");
        for (EdiSegment item : segmentList) {
            rff.clear();
            item.fillBean(rff, null, null, null, null, "transType");
            String transType = rff.getString("transType");
            if ("6".equals(transType)) {
                // 空运: TDT+20+{FlightNumber}++6+{AirlineCode}::3'
                item.fillBean(rff, null, null, "flight", null, "transType", "airlineCode");
                re.getInfoMap().put("transType", "air");
                re.getInfoMap().put("flight", rff.getString("flight"));
                re.getInfoMap().put("airlineCode", rff.getString("airlineCode"));
            } else if ("11".equals(transType)) {
                // 海运: TDT+20+{VoyageNumber}++11++++{VesselId}::11'
                item.fillBean(rff, null, null, "voyage", null, "transType", null, null, null, "vessel");
                re.getInfoMap().put("transType", "sea");
                re.getInfoMap().put("voyage", rff.getString("voyage"));
                re.getInfoMap().put("vessel", rff.getString("vessel"));
            }
        }

        // 解析 LOC	报文行 (C-99) : 地点信息
        segmentList = finder.nextAll(true, "LOC");
        for (EdiSegment item : segmentList) {
            rff.clear();
            item.fillBean(rff, null, "funcCode", "funcVal");
            String funcCode = rff.getString("funcCode");
            if ("12".equals(funcCode)) {
                re.getInfoMap().put("dischargePort", rff.getString("funcVal"));
            } else if ("4".equals(funcCode)) {
                re.getInfoMap().put("goodsPlace", rff.getString("funcVal"));
            }
        }

        // 解析 GIS 报文行 (C-10)
        segmentList = finder.nextAll(true, "GIS");
        for (EdiSegment item : segmentList) {
            rff.clear();
            item.fillBean(rff, null, "priority,idCode,agencyCode");
            String idCode = rff.getString("idCode");
            String agencyCode = rff.getString("agencyCode");
            if ("110".equals(idCode) && "95".equals(agencyCode)) {
                re.getInfoMap().put("priority", rff.getString("priority"));
            }
        }

        // 解析 NAD 报文行 (C-10)
        segmentList = finder.nextAll(true, "NAD");
        for (EdiSegment item : segmentList) {
            rff.clear();
            item.fillBean(rff, null, "funcCode", "partyId");
            String funcCode = rff.getString("funcCode");
            if ("MR".equals(funcCode)) {
                re.getInfoMap().put("mlSiteId", rff.getString("partyId"));
            } else if ("UD".equals(funcCode)) {
                // would be ML abn code
                re.getInfoMap().put("mlClientId", rff.getString("partyId"));
            }
        }

        // 解析 RFF 报文行 (C-999 --> M-1)
        segmentList = finder.nextAll(true, "RFF");
        for (EdiSegment item : segmentList) {
            rff.clear();
            item.fillBean(rff, null, "refCode,refVal,,refVer");
            String refCode = rff.getString("refCode");
            if ("AAQ".equals(refCode)) {
                re.getRefMap().put("cntrNum", rff.getString("refVal"));
            } else if ("ABO".equals(refCode)) {
                String referId = rff.getString("refVal");
                re.getRefMap().put("senderRef", rff.getString("refVal"));
                if (referId != null) {
                    re.setRefId(referId);
                    re.setRefIdInLower(referId.toLowerCase());
                    re.setRefVer(rff.getInt("refVer"));
                }
            } else if ("ABT".equals(refCode)) {
                re.getRefMap().put("importDecId", rff.getString("refVal"));
            } else if ("BH".equals(refCode)) {
                re.getRefMap().put("houseBill", rff.getString("refVal"));
                re.getRefMap().put("transType", "sea");
            } else if ("MB".equals(refCode)) {
                re.getRefMap().put("masterBill", rff.getString("refVal"));
                re.getRefMap().put("transType", "sea");
            } else if ("HWB".equals(refCode)) {
                re.getRefMap().put("houseBill", rff.getString("refVal"));
                re.getRefMap().put("transType", "air");
            } else if ("MWB".equals(refCode)) {
                re.getRefMap().put("masterBill", rff.getString("refVal"));
                re.getRefMap().put("transType", "air");
            }
        }

        return re;
    }
}
