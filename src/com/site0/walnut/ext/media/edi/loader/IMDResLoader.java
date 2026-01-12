package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.imd.*;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import java.math.BigDecimal;
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
        boolean findFtx = finder.moveToUtil("FTX", true, "NAD", "RFF");
        if (findFtx) {
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
        boolean findGis = finder.moveToUtil("GIS", true, "NAD", "RFF");
        if (findGis) {
            segs = finder.nextAll(true, "GIS");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "indDescCode,indCode,agencyCode");
                if (rff.is("agencyCode", "95")) {
                    if (rff.is("indCode", "117")) {
                        re.setPreLodge(rff.is("indDescCode", "Y"));
                    } else if (rff.is("indCode", "109")) {
                        String indDescCode = rff.getString("indDescCode");
                        if ("TLB".equalsIgnoreCase(indDescCode)) {
                            re.setTlb(true);
                        } else if ("LLB".equalsIgnoreCase(indDescCode)) {
                            re.setLlb(true);
                        }
                    }
                }
            }
        }

        // 定位到 NAD 报文行： NAD+{FuncCode}+{PartyId}::{AgencyCode}+{name1}:{name2}:{boxNum}'
        boolean findNad = finder.moveToUtil("NAD", true, "RFF");
        if (findNad) {
            segs = finder.nextAll(true, "NAD");
            for (EdiSegment item : segs) {
                rff.clear();
                item.fillBean(rff, null, "funcCode", "partyId,,agencyCode", "name1,name2,boxNum");
                if (rff.is("funcCode", "MR")) {
                    re.setMsgRecipient(rff.getString("partyId"));
                } else if (rff.is("funcCode", "VT")) {
                    re.setBranchId(rff.getString("partyId"));
                } else if (rff.is("funcCode", "CB")) {
                    if (rff.is("agencyCode", "95")) {
                        re.setBrokerLicNum(rff.getString("partyId"));
                    } else {
                        String name = (rff.getString("name1", "") + rff.getString("name2", "")).trim();
                        if (!name.isEmpty()) {
                            re.setBrokerName(name);
                        }
                        String boxNum = rff.getString("boxNum");
                        if (boxNum != null && !boxNum.trim().isEmpty()) {
                            re.setBorkerBoxNum(boxNum.trim());
                        }
                    }
                } else if (rff.is("funcCode", "IM")) {
                    String name = (rff.getString("name1", "") + rff.getString("name2", "")).trim();
                    if (!name.isEmpty()) {
                        re.setImporterName(name);
                    }
                    String boxNum = rff.getString("boxNum");
                    if (boxNum != null && !boxNum.trim().isEmpty()) {
                        re.setImporterBoxNum(boxNum.trim());
                    }
                }
            }
        }

        // 定位到 RFF 报文行
        boolean findRff = finder.moveToUtil("RFF", true);
        if (findRff) {
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

        // 定位的 DOC 报文行
        boolean findDoc = finder.moveTo("DOC", true);
        List<ImdResTransLine> transLines = new ArrayList<>();

        if (findDoc) {
            finder.reset();
            List<ImdReplyHeadErr> headErrs = this.findHeadErrs(finder, findDoc);
            re.setHeadErrs(headErrs);

            finder.reset();
            String stopTag = "DOC";
            boolean findTax = finder.moveToUtil("TAX", true, stopTag);
            // 解析 Segment Group 5: TAX-MOA
            if (findTax) {
                segs = finder.nextAllUntilStopTag(true, "MOA", stopTag);
                List<Map<String, String>> heaMoas = new ArrayList<>();
                if (!segs.isEmpty()) {
                    for (EdiSegment seg : segs) {
                        Map<String, String> map = new HashMap<>();
                        NutBean bean = seg.getBean(null, "taxType,taxAmount");
                        map.put(bean.getString("taxType"), bean.getString("taxAmount"));
                        heaMoas.add(map);
                    }
                    if (!heaMoas.isEmpty()) {
                        re.setHeadMoas(heaMoas);
                    }
                }
            }
            while (findDoc) {
                // 解析 Segment Group 6: DOC-FTX-SG11-SG13
                ImdResTransLine transLine = new ImdResTransLine();
                EdiSegment segment = finder.next("DOC");
                int lineNum = -1;
                if (segment != null) {
                    NutBean bean = segment.getBean(null, "triggerVal", "lineNum");
                    if (bean.is("triggerVal", "1") && bean.has("lineNum")) {
                        lineNum = bean.getInt("lineNum");
                    }
                }
                if (lineNum != -1) {
                    transLine.setLineNum(lineNum);
                    List<EdiSegment> segments = finder.nextAll(true, "FTX");
                    for (EdiSegment seg : segments) {
                        if (seg.isRawContentStartsWith("FTX+AHN")) {
                            NutBean bean = seg.getBean(null, null, null, null, "statusType,statusDesc");
                            transLine.setStTp(bean.getString("statusType"));
                            transLine.setStDesc(bean.getString("statusDesc"));
                            break;
                        }
                    }
                }
                transLines.add(transLine);

                // 解析可能有的 所有 entry lines 的信息
                boolean findCst = finder.moveTo("CST", true);
                if (findCst) {
                    List<ImdResEntryLine> entryLines = new ArrayList<>();
                    List<EdiSegment> ediSegmentList = finder.nextAllUntilStopTag(true, new String[]{"CST", "FTX", "TAX", "MOA"}, new String[]{"CST", "ERP", "DOC", "UNT"});
                    while (ediSegmentList != null && ediSegmentList.size() > 0) {
                        ImdResEntryLine entryLine = new ImdResEntryLine();
                        List<Map<String, String>> dutyRates = new ArrayList<>();
                        for (EdiSegment item : ediSegmentList) {
                            if (item.isTag("CST")) {
                                NutBean nutBean = item.getBean(null, "LineNumber", "natureType");
                                entryLine.setLineNum(nutBean.getInt("LineNumber"));
                                entryLine.setNatureType(nutBean.getString("natureType"));
                            } else if (item.isTag("FTX")) {
                                // FTX+AAF+++FREE
                                NutBean nutBean = item.getBean(null, null, null, null, "dutyRateDesc");
                                entryLine.setDutyRateDesc(nutBean.getString("dutyRateDesc"));
                            } else if (item.isTag("MOA")) {
                                // MOA+40:0000000027457.31
                                NutBean nutBean = item.getBean(null, "amountType,amountValue");
                                String amountValue = nutBean.getString("amountValue");
                                if (amountValue != null && !amountValue.trim().isEmpty()) {
                                    amountValue = new BigDecimal(amountValue).stripTrailingZeros().toPlainString();
                                }
                                Map<String, String> map = new HashMap<>();
                                map.put(nutBean.getString("amountType"), amountValue);
                                dutyRates.add(map);
                            }
                        }
                        if (dutyRates.size() > 0) {
                            entryLine.setDutyRates(dutyRates);
                        }
                        entryLines.add(entryLine);
                        ediSegmentList = finder.nextAllUntilStopTag(true, new String[]{"CST", "FTX", "TAX", "MOA"}, new String[]{"CST", "ERP", "DOC", "UNT"});
                    }
                    transLine.setEntryLines(entryLines);
                }

                // 解析 "Segment Group 13: ERP-ERC-FTX"
                List<ImdReplyLineErr> lineErrs = this.findTransLineErrs(finder);
                transLine.setLineErrs(lineErrs);

                // 下一轮的 Segment Group 6: DOC-FTX-SG11-SG13
                findDoc = finder.moveTo("DOC", true);
            }
            if (transLines.size() > 0) {
                re.setTransLines(transLines);
            }

        } else { // 没有 DOC 报文行的情况
            finder.reset();
            List<ImdReplyHeadErr> headErrs = this.findHeadErrs(finder, findDoc);
            re.setHeadErrs(headErrs);

            finder.reset();
            String stopTag = "UNT";
            boolean findTax = finder.moveToUtil("TAX", true, stopTag);
            // 解析 Segment Group 5: TAX-MOA
            if (findTax) {
                segs = finder.nextAllUntilStopTag(true, "MOA", stopTag);
                List<Map<String, String>> heaMoas = new ArrayList<>();
                if (!segs.isEmpty()) {
                    for (EdiSegment seg : segs) {
                        Map<String, String> map = new HashMap<>();
                        NutBean bean = seg.getBean(null, "taxType,taxAmount");
                        map.put(bean.getString("taxType"), bean.getString("taxAmount"));
                        heaMoas.add(map);
                    }
                    if (!heaMoas.isEmpty()) {
                        re.setHeadMoas(heaMoas);
                    }
                }
            }
        }

        if (transLines.size() > 0) {
            re.setTransLines(transLines);
        }

        return re;
    }

    private List<ImdReplyHeadErr> findHeadErrs(EdiSegmentFinder finder, boolean findDoc) {
        List<ImdReplyHeadErr> headErrs = new ArrayList<>();
        // 定位到 TAX/CST 之前的 ERP-ERC-FTX 报文组，解析错误信息
        String stopTag;
        if (findDoc) {
            stopTag = "TAX";
        } else {
            stopTag = "UNT";
        }
        boolean find = finder.moveToUtil("ERP", true, stopTag);
        while (find) {
            // 找到 ERP-ERC-FTX 报文组
            List<EdiSegment> errs = finder.findContinueSegments("ERP", "^(ERP|ERC|FTX)$", "^(ERP|TAX)$");
            // 看来找不到错误了，那么退出循环
            if (errs.isEmpty() || !errs.get(0).is("ERP")) {
                break;
            }

            boolean isHeadErrs = true;
            for (EdiSegment seg : errs) {
                if (seg.isRawContentStartsWith("FTX+") && !seg.isRawContentStartsWith("FTX+AAO")) {
                    isHeadErrs = false;
                }
            }

            if (isHeadErrs) {
                headErrs.add(new ImdReplyHeadErr(errs));
            }
            find = finder.moveToUtil("ERP", true, "TAX");
        }
        return headErrs.isEmpty() ? null : headErrs;
    }

    private List<ImdReplyLineErr> findTransLineErrs(EdiSegmentFinder finder) {
        List<ImdReplyLineErr> tailErrs = new ArrayList<>();
        // 定位到 TAX 之前的 ERP-ERC-FTX 报文组，解析错误信息
        boolean find = finder.moveToUtil("ERP", true, "UNT");
        while (find) {
            // 找到 ERP-ERC-FTX 报文组
            List<EdiSegment> errs = finder.findContinueSegments("ERP", "^(ERP|ERC|FTX)$", "^(ERP|DOC|CNT|UNT)$");
            // 看来找不到错误了，那么退出循环
            if (errs.isEmpty() || !errs.get(0).is("ERP")) {
                break;
            }

            boolean isTailErrs = true;
            for (EdiSegment seg : errs) {
                if (seg.isRawContentStartsWith("FTX+T") && !seg.isRawContentStartsWith("FTX+AAT")) {
                    isTailErrs = false;
                }
            }

            if (isTailErrs) {
                tailErrs.add(new ImdReplyLineErr(errs));
            }
            find = finder.moveToUtil("ERP", true, "UNT");
        }
        return tailErrs.isEmpty() ? null : tailErrs;
    }

}
