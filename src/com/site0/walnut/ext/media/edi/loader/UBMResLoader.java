package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.ubm.IcsReplyUbmRes;
import com.site0.walnut.ext.media.edi.msg.reply.ubm.UbmLineRst;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UBMResLoader implements EdiMsgLoader<IcsReplyUbmRes> {
    @Override
    public Class<IcsReplyUbmRes> getResultType() {
        return IcsReplyUbmRes.class;
    }

    @Override
    public IcsReplyUbmRes load(EdiMessage msg) {
        IcsReplyUbmRes re = new IcsReplyUbmRes();

        re.setRstVer(0);
        // BGM 报文中的 FUNCTION CODE, 此报文固定为 32: Approval
        re.setFuncCode(32);
        // 改报文固定为 true
        re.setSuccess(true);

        EdiSegmentFinder finder = msg.getFinder();
        NutMap rff = new NutMap();
        List<EdiSegment> segs;

        /**
         * 定位到 BGM 报文行，解析 FuncCode
         * BGM+961:::UBMREQR+2A91 DI61 J6AE:1+32'
         */
        IcsLoaderHelper.fillResFuncCode(re, finder);

        // 解析 DTM 报文行, DTM+9:20240612215114156647:ZZZ'
        boolean find = finder.moveTo(true, "DTM", "RFF", "DOC");
        if (find) {
            segs = finder.nextAll(true, "DTM");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "funcCode,dateTime");
                if (rff.is("funcCode", "9")) {
                    re.setTxnTime(rff.getString("dateTime"));
                }
            }
        }

        // 解析 FTX 报文行, FTX+AAH+++FJM396HU00000337/PRD1' 等
        find = finder.moveTo(true, "FTX", "RFF", "DOC");
        if (find) {
            segs = finder.nextAll(true, "FTX");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "subjectCode", null, null, "freeText");
                if (rff.is("subjectCode", "AAH")) {
                    re.setCargoMsgId(rff.getString("freeText"));
                } else if (rff.is("subjectCode", "ACF")) {
                    re.setMvTypeText(rff.getString("freeText"));
                }
            }
        }

        // 解析 TDT 报文行
        find = finder.moveTo(true, "TDT", "RFF", "DOC");
        if (find) {
            segs = finder.nextAll(true, "TDT");
            for (EdiSegment item : segs) {
                rff.clear();
                // 1: Inland transport , 20: Main-carriage transport
                if (item.isRawContentStartsWith("TDT+1+")) {
                    rff.clear();
                    // TDT+1++{MoveMode}', 示例: TDT+1++ROA'
                    // TDT+1+{UnderbondVoyNum}+{MoveMode}+++++{UnderbondSeaId}::11', 无示例
                    item.fillBean(rff, null, "stageCode", "ubmVoyNum", "mvMode", null, null, null, null, "ubmSeaId");
                    String ubmVoyNum = rff.getString("ubmVoyNum");
                    String mvMode = rff.getString("mvMode");
                    String ubmSeaId = rff.getString("ubmSeaId");
                    Map<String, String> inTrans = re.getInTrans();
                    if (Strings.isNotBlank(mvMode)) {
                        inTrans.put("mvMode", mvMode);
                    }
                    if (Strings.isNotBlank(ubmVoyNum)) {
                        inTrans.put("ubmVoyNum", ubmVoyNum);
                    }
                    if (Strings.isNotBlank(ubmSeaId)) {
                        inTrans.put("ubmSeaId", ubmSeaId);
                    }
                } else if (item.isRawContentStartsWith("TDT+20+")) {
                    Map<String, String> mainTrans = re.getMainTrans();
                    // TDT+20+{FltNum}++6+{AirlineCode}::3'
                    // TDT+20+{VoyNum}++11++++{VesselId}::11'
                    rff.clear();
                    item.fillBean(rff, null, null, null, null, "transType");
                    if (rff.is("transType", "6")) {
                        rff.clear();
                        item.fillBean(rff, null, null, "fltNum", null, "transType", "airlineCode");
                        mainTrans.put("fltNum", rff.getString("fltNum"));
                        mainTrans.put("transType", rff.getString("transType"));
                        mainTrans.put("airlineCode", rff.getString("airlineCode"));
                    } else if (rff.is("transType", "11")) {
                        rff.clear();
                        item.fillBean(rff, null, null, "voyNum", null, "transType", null, null, null, "vesselId");
                        mainTrans.put("voyNum", rff.getString("voyNum"));
                        mainTrans.put("transType", rff.getString("transType"));
                        mainTrans.put("vesselId", rff.getString("vesselId"));
                    }
                }
            }
        }

        // 解析 LOC 报文行, 示例: LOC+5+FM27N::95' , LOC+4+GE65A::95'
        find = finder.moveTo(true, "LOC", "RFF", "DOC");
        if (find) {
            segs = finder.nextAll(true, "LOC");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "locType", "locCode");
                String locType = rff.getString("locType");
                String locCode = rff.getString("locCode");
                if ("4".equals(locType)) {
                    re.getLocInfo().put("discEstId", locCode);
                } else if ("5".equals(locType)) {
                    re.getLocInfo().put("oriEstId", locCode);
                } else if ("6".equals(locType)) {
                    re.getLocInfo().put("destNextPort", locCode);
                }
            }
        }

        // 略过 解析 NAD 报文行, 示例: NAD+MR+FJM396H::95' , NAD+UD+74609780707::95'
        find = finder.moveTo(true, "NAD", "RFF", "DOC");
        if (find) {
            finder.nextAll(true, "NAD");
        }

        // 解析 RFF 报文行, 示例: RFF+ACW:UBMREQ', 示例: RFF+ABO:U00000337/PRD1::1', RFF+ANX:UNDERBOND APPROVAL', RFF+ACD:DCL'
        find = finder.moveTo(true, "RFF", "DOC");
        if (find) {
            segs = finder.nextAll(true, "RFF");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "refCode,refVal,,refVer");
                String refCode = rff.getString("refCode");
                String refVal = rff.getString("refVal");
                if ("ABO".equals(refCode)) {
                    if (Strings.isNotBlank(refVal)) {
                        re.setRefId(refVal);
                        re.setRefIdInLower(refVal.toLowerCase());
                        re.setRefVer(rff.getInt("refVer"));
                    }
                } else if ("ACD".equals(refCode)) {
                    re.setReqReason(refVal);
                } else if ("AIO".equals(refCode)) {
                    re.setTransShipNum(refVal);
                } else if ("ANX".equals(refCode)) {
                    re.setUbmNotice(refVal);
                }
            }
        }

        // 查找 Segment Group 6: DOC-PAC-RFF-PCI-FTX
        List<UbmLineRst> lineList = new ArrayList<>();
        while (!finder.isEnd()) {
            List<EdiSegment> lines = finder.findContinueSegments("DOC", "^(PAC|RFF|PCI|FTX)$", "^(DOC|UNT)$");
            if (lines.isEmpty()) {
                break;
            }
            UbmLineRst lineRst = new UbmLineRst(lines);
            lineList.add(lineRst);
        }
        re.setLineRsts(lineList);
        return re;
    }
}
























