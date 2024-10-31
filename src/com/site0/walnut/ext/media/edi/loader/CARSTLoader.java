package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.CargoStaAdviceObj;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import org.nutz.lang.util.NutMap;

import java.util.List;

public class CARSTLoader implements EdiMsgLoader<CargoStaAdviceObj> {


    @Override
    public Class<CargoStaAdviceObj> getResultType() {
        return CargoStaAdviceObj.class;
    }

    @Override
    public CargoStaAdviceObj load(EdiMessage msg) {
        CargoStaAdviceObj re = new CargoStaAdviceObj();

        EdiSegmentFinder finder = msg.getFinder();
        NutMap rff = new NutMap();

        // 定位到 BGM 报文行
        finder.next("BGM");

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

        // 解析 FTX+AHN 报文行
        segmentList = finder.nextAll(true, "FTX");
        for (EdiSegment item : segmentList) {
            rff.clear();
            item.fillBean(rff, null, "subjectCode", null, null, "stName,stVal");
            if (rff.is("subjectCode", "AHN")) {
                re.getStatusMap().put(rff.getString("stName"), rff.getString("stVal"));
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
                re.getInfoMap().put("transType", rff.getString("6"));
                re.getInfoMap().put("flight", rff.getString("flight"));
                re.getInfoMap().put("airlineCode", rff.getString("airlineCode"));
            } else if ("11".equals(transType)) {
                // 海运: TDT+20+{VoyageNumber}++11++++{VesselId}::11'
                item.fillBean(rff, null, null, "voyage", null, "transType", null, null, null, "vessel");
                re.getInfoMap().put("transType", rff.getString("11"));
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
            item.fillBean(rff, null, "refCode,refVal,refVer");

        }

        return null;
    }
}
