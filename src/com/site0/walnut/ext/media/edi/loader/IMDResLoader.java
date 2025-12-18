package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.imd.IcsReplyImdRes;
import com.site0.walnut.ext.media.edi.msg.reply.imd.ImdReplyHeadErr;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        boolean find = finder.moveToUtil("DTM", true, "NAD", "RFF");
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
        find = finder.moveToUtil("FTX", true, "NAD", "RFF");
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
        find = finder.moveToUtil("GIS", true, "NAD", "RFF");
        if (find) {
            segs = finder.nextAll(true, "GIS");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "indDescCode,indCode,agencyCode");
                if (rff.is("indDescCode", "95")) {
                    if (rff.is("indCode", "117")) {
                        if (rff.is("indDescCode", "Y")) {
                            re.setPreLodge(true);
                        } else {
                            re.setPreLodge(false);
                        }
                    }
                }
            }
        }

        // 定位到 NAD 报文行： NAD+{FuncCode}+{PartyId}::{AgencyCode}+{name1}:{name2}:{boxNum}'
        find = finder.moveToUtil("NAD", true, "RFF");
        if (find) {
            segs = finder.nextAll(true, "NAD");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "funcCode", "partyId,,agencyCode" + "name1,name2,boxNum");
                if (rff.is("funcCode", "MR")) {
                    re.setMsgRecipient(rff.getString("partyId"));
                } else if (rff.is("funcCode", "VT")) {
                    re.setBranchId(rff.getString("partyId"));
                } else if (rff.is("funcCode", "CB")) {
                    if (rff.is("agencyCode", "95")) {
                        re.setBrokerLicNum(rff.getString("partyId"));
                    } else {
                        String name = (rff.getString("name1", "") + rff.getString("name2", "")).trim();
                        if (name != null && name.length() > 0) {
                            re.setBrokerName(name);
                        }
                        String boxNum = rff.getString("boxNum");
                        if (boxNum != null && boxNum.trim().length() > 0) {
                            re.setBorkerBoxNum(boxNum.trim());
                        }
                    }
                } else if (rff.is("funcCode", "IM")) {
                    // todo
                    String name = (rff.getString("name1", "") + rff.getString("name2", "")).trim();
                    if (name != null && name.length() > 0) {
                        re.setImporterName(name);
                    }
                    String boxNum = rff.getString("boxNum");
                    if (boxNum != null && boxNum.trim().length() > 0) {
                        re.setImporterBoxNum(boxNum.trim());
                    }
                }
            }
        }

        // 定位到 RFF 报文行
        find = finder.moveToUtil("RFF", true);
        if (find) {
            segs = finder.nextAll(true, "RFF");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "refCode,refVal,,refVer");
                String refCode = rff.getString("refCode");
                String refVal = rff.getString("refVal");
                if ("AAE".equals(refCode)) {
                    re.setHeaderNatureType(refVal);
                } else if ("ABO".equals(refCode)) {
                    if (Strings.isNotBlank(refVal)) {
                        re.setRefId(refVal);
                        re.setRefIdInLower(refVal.toLowerCase());
                        re.setRefVer(rff.getInt("refVer"));
                    }
                } else if ("ABQ".equals(refCode)) {
                    re.setImporterRef(refVal);
                } else if ("ABT".equals(refCode)) {
                    re.setImpDecNum(refVal);
                    re.setImpDecVer(rff.getString("refVer"));
                } else if ("ADU".equals(refCode)) {
                    re.setBrokerRef(refVal);
                }
            }
        }


        // 定位到 TAX 之前的 ERP-ERC-FTX 报文组，解析错误信息
        boolean findTax = finder.moveToUtil("TAX", true, "DOC", "CST");
        finder.reset();
        if (findTax) {
            List<ImdReplyHeadErr> headErrs = this.findHeadErrs(finder);
            re.setHeadErrs(headErrs);
        }

        // 解析 Segment Group 5: TAX-MOA todo xxx
        boolean findCst = finder.moveToUtil("CST", true, "UNT");
        finder.reset();
        if (findCst) {
            segs = finder.nextAllUntilStopTag(true, "MOA", "CST");
            List<Map<String, String>> heaMoas = new ArrayList<>();
            if (!segs.isEmpty()) {
                for (EdiSegment seg : segs) {
                    Map<String, String> map = new HashMap<>();
                    // MOA+{HeaderAmountType}:{HeaderAmount}'
                    NutBean bean = seg.getBean(null, "taxType,taxAmount");
                    map.put(bean.getString("taxType"), bean.getString("taxAmount"));
                    heaMoas.add(map);
                }
                if (!heaMoas.isEmpty()) {
                    re.setHeadMoas(heaMoas);
                }
            }
        }

        // 解析 Segment Group 6: DOC-FTX-SG11-SG13 的 DOC-FTX todo
        if (findCst) {
            find = finder.moveToUtil("DOC", true, "CST");
            if (find) {
                // todo
                EdiSegment segment = finder.next("DOC");
                if (segment != null) {
                    // todo DOC+1+{TransportLineNum}'
                    NutBean bean =  segment.getBean(null,"triggerVal", "lineNum");
                    if (bean.is("triggerVal","1") && bean.has("lineNum")) {

                    }
                }
            }
        }

        return null;
    }

    private List<ImdReplyHeadErr> findHeadErrs(EdiSegmentFinder finder) {
        List<ImdReplyHeadErr> headErrs = new ArrayList<>();
        // 定位到 TAX 之前的 ERP-ERC-FTX 报文组，解析错误信息
        boolean find = finder.moveToUtil("ERP", true, "TAX");
        while (find) {
            // 找到 ERP-ERC-FTX 报文组
            List<EdiSegment> errs = finder.findContinueSegments("ERP", "^(ERC|FTX)$", "^(ERP|TAX)$");
            // 看来找不到错误了，那么退出循环
            if (errs.isEmpty() || !errs.get(0).is("ERC")) {
                break;
            }
            if (isHeadErr(errs)) {
                headErrs.add(new ImdReplyHeadErr(errs));
            }
            find = finder.moveToUtil("ERP", true, "TAX");
        }
        return headErrs.size() == 0 ? null : headErrs;
    }

    private boolean isHeadErr(List<EdiSegment> errs) {
        for (EdiSegment seg : errs) {
            if (seg.isRawContentStartsWith("FTX+AAO")) {
                return true;
            }
        }
        return false;
    }

}
