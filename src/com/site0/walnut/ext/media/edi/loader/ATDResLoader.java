package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.atd.IcsReplyAtdRes;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.util.HashMap;
import java.util.List;

public class ATDResLoader implements EdiMsgLoader<IcsReplyAtdRes> {
    @Override
    public Class<IcsReplyAtdRes> getResultType() {
        return IcsReplyAtdRes.class;
    }

    @Override
    public IcsReplyAtdRes load(EdiMessage msg) {
        IcsReplyAtdRes re = new IcsReplyAtdRes();
        re.setRstVer(0);
        re.setFuncCode(11);

        EdiSegmentFinder finder = msg.getFinder();
        NutMap bean = new NutMap();
        List<EdiSegment> segs;

        IcsLoaderHelper.fillResFuncCode(re, finder);

        boolean found = finder.moveToUtil("DTM", true, "FTX", "TDT", "LOC", "GIS", "NAD", "RFF", "DOC", "CNT", "UNT");
        if (found) {
            segs = finder.nextAll(true, "DTM");
            for (EdiSegment item : segs) {
                bean.clear();
                item.fillBean(bean, null, "funcCode,dateTime,formatCode");
                if (!bean.is("formatCode", "102")) {
                    continue;
                }
                String dt = bean.getString("dateTime");
                if (bean.is("funcCode", "58")) {
                    re.setClearDate(dt);
                } else if (bean.is("funcCode", "138")) {
                    re.setPaymentDate(dt);
                } else if (bean.is("funcCode", "234")) {
                    re.setCollectStartDate(dt);
                } else if (bean.is("funcCode", "235")) {
                    re.setCollectEndDate(dt);
                }
            }
        }

        finder.reset();
        found = finder.moveToUtil("FTX", true, "RFF", "DOC");
        if (found) {
            segs = finder.nextAll(true, "FTX");
            for (EdiSegment item : segs) {
                bean.clear();
                item.fillBean(bean, null, "subjectCode", null, null, "text1,text2");
                String subjectCode = bean.getString("subjectCode");
                if ("AHN".equals(subjectCode)) {
                    String stType = bean.getString("text1");
                    String stDesc = bean.getString("text2");
                    if (Strings.isNotBlank(stType)) {
                        if (re.getAtdStatus() == null) {
                            re.setAtdStatus(new HashMap<>());
                        }
                        re.getAtdStatus().put(stType, stDesc);
                    }
                } else if ("CIP".equals(subjectCode)) {
                    String text = bean.getString("text1");
                    re.setActionReason(text);
                } else if ("MKS".equals(subjectCode)) {
                    String text = bean.getString("text1");
                    re.setTransMarksNums(text);
                }
            }
        }

        // ## TDT Details of Transport C9 报文行
        finder.reset();
        found = finder.moveToUtil("TDT", true, "DOC", "UNT");
        if (found) {
            segs = finder.nextAll(true, "TDT");
            for (EdiSegment item : segs) {
                if (!item.isRawContentStartsWith("TDT+20")) {
                    continue;
                }
                bean.clear();
                item.fillBean(bean, null, "stageCode", "voyageNum", "transportMode", null, "airlineCode", null, null, "vesselId");
                re.setVoyageNum(bean.getString("voyageNum"));
                re.setTransportMode(bean.getString("transportMode"));
                re.setAirlineCode(bean.getString("airlineCode"));
                re.setVesselId(bean.getString("vesselId"));
            }
        }

        // ## LOC Place/Location Identification C99 报文行
        finder.reset();
        found = finder.moveToUtil("LOC", true, "DOC", "UNT");
        if (found) {
            segs = finder.nextAll(true, "LOC");
            for (EdiSegment item : segs) {
                bean.clear();
                item.fillBean(bean, null, "locType", "locCode");
                String locType = bean.getString("locType");
                String locCode = bean.getString("locCode");
                if ("12".equals(locType)) {
                    re.setDischargePort(locCode);
                } else if ("18".equals(locType)) {
                    re.setWarehouseEstId(locCode);
                }
            }
        }

        // ## GIS General Indicator C10 报文行
        finder.reset();
        found = finder.moveToUtil("GIS", true, "NAD", "RFF", "DOC", "CNT", "UNT");
        if (found) {
            segs = finder.nextAll(true, "GIS");
            for (EdiSegment item : segs) {
                bean.clear();
                item.fillBean(bean, null, "indCode");
                re.setActionIndicator(bean.getString("indCode"));
            }
        }

        // ## SG1-NAD Name and Address M1 报文行
        finder.reset();
        found = finder.moveToUtil("NAD", true, "DOC", "UNT");
        if (found) {
            segs = finder.nextAll(true, "NAD");
            for (EdiSegment item : segs) {
                bean.clear();
                item.fillBean(bean, null, "funcCode", "partyId,,agencyCode", "name1,name2,boxNum");
                String funcCode = bean.getString("funcCode");
                if ("MR".equals(funcCode)) {
                    re.setMsgRecipient(bean.getString("partyId"));
                } else if ("VT".equals(funcCode)) {
                    re.setBranchId(bean.getString("partyId"));
                } else if ("CB".equals(funcCode)) {
                    if (bean.is("agencyCode", "95")) {
                        re.setBrokerLicNum(bean.getString("partyId"));
                    }

                    String name = null;
                    if (bean.getString("name1") != null) {
                        name = bean.getString("name1");
                    }
                    if (bean.getString("name2") != null) {
                        if (name == null) {
                            name = bean.getString("name2");
                        } else {
                            name = name + bean.getString("name2");
                        }
                    }
                    re.setBrokerName(name);

                    String boxNum = bean.getString("boxNum");
                    if (boxNum != null) {
                        re.setBorkerBoxNum(boxNum);
                    }
                } else if ("CM".equals(funcCode)) {
                    String name1 = bean.getString("name1");
                    String name2 = bean.getString("name2");
                    String boxNum = bean.getString("boxNum");
                    if (Strings.isNotBlank(name1)) {
                        re.setCustomsState(name1.trim());
                    }
                    if (Strings.isNotBlank(name2)) {
                        re.setWorkgroupName(name2.trim());
                    }
                    if (Strings.isNotBlank(boxNum)) {
                        re.setCustomsOfficer(boxNum.trim());
                    }
                } else if ("IM".equals(funcCode)) {
                    String name = (bean.getString("name1", "") + bean.getString("name2", "")).trim();
                    if (Strings.isNotBlank(name)) {
                        re.setImporterName(name);
                    }
                    String boxNum = Strings.sNull(bean.getString("boxNum")).trim();
                    if (Strings.isNotBlank(boxNum)) {
                        re.setImporterBoxNum(boxNum);
                    }
                }
            }
        }

        // ## SG2-CTA Contact Information M1 报文行
        finder.reset();
        found = finder.moveToUtil("COM", true, "RFF", "DOC", "UNT");
        if (found) {
            segs = finder.nextAll(true, "COM");
            for (EdiSegment seg : segs) {
                NutMap comBean = new NutMap();
                seg.fillBean(comBean, null, "communicationNumber,communicationCode");
                String communicationNumber = Strings.sNull(comBean.getString("communicationNumber"));
                String communicationCode = Strings.sNull(comBean.getString("communicationCode"));
                if (Strings.isBlank(communicationNumber) || Strings.isBlank(communicationCode)) {
                    continue;
                }
                if ("EM".equals(communicationCode)) {
                    re.setCustomsEmail(communicationNumber);
                } else if ("FX".equals(communicationCode)) {
                    re.setCustomsFaxNum(communicationNumber);
                } else if ("TE".equals(communicationCode)) {
                    re.setCustomsTel(communicationNumber);
                }
            }
        }

        // ## SG3-RFF Reference M1 报文行
        found = finder.moveToUtil("RFF", true, "DOC");
        if (found) {
            segs = finder.nextAll(true, "RFF");
            for (EdiSegment item : segs) {
                bean.clear();
                item.fillBean(bean, null, "refCode,refVal,,refVer");
                String refCode = bean.getString("refCode");
                String refVal = bean.getString("refVal");
                if ("AAE".equals(refCode)) {
                    re.setNatureType(refVal);
                } else if ("ABO".equals(refCode)) {
                    if (Strings.isNotBlank(refVal)) {
                        re.setRefId(refVal);
                        re.setRefIdInLower(refVal.toLowerCase());
                        re.setRefVer(bean.getInt("refVer"));
                    }
                } else if ("ABQ".equals(refCode)) {
                    re.setImporterRef(refVal);
                } else if ("ABT".equals(refCode)) {
                    re.setCusDecNum(refVal);
                    re.setCusDecVer(bean.getString("refVer"));
                } else if ("ADU".equals(refCode)) {
                    re.setBrokerRef(refVal);
                } else if ("AIA".equals(refCode)) {
                    re.setDealSecurityCode(refVal);
                } else if ("REN".equals(refCode)) {
                    re.setGoodsReceiptId(refVal);
                }
            }
        }

        // 默认为 true
        re.setSuccess(true);

        // 我们不解析 Segment Group 12: TAX-MOA-MEA-RFF，并且认为不应该有 MOA 报文行
        finder.reset();
        found = finder.moveToUtil("MOA", true, "UNT");
        if (found) {
            re.setSuccess(false);
        }

        // 我们不解析 Segment Group 13: ERP-ERC-FTX 报文组，并且认为不应该有 ERP，ERC 报文行
        finder.reset();
        found = finder.moveToUtil("ERP", true, "UNT");
        if (found) {
            re.setSuccess(false);
        }
        if (re.isSuccess()) {
            finder.reset();
            found = finder.moveToUtil("ERC", true, "UNT");
            if (found) {
                re.setSuccess(false);
            }
        }

        // ## CNT Control Total	C9 报文行
        finder.reset();
        found = finder.moveToUtil("CNT", true, "UNT");
        if (found) {
            segs = finder.nextAll(true, "CNT");
            for (EdiSegment item : segs) {
                bean.clear();
                item.fillBean(bean, null, "cntType,cntVal");
                String cntType = bean.getString("cntType");
                if ("2".equals(cntType)) {
                    re.setCntLine(bean.getInt("cntVal"));
                } else if ("3".equals(cntType)) {
                    re.setCntLineAndSubItem(bean.getInt("cntVal"));
                } else if ("5".equals(cntType)) {
                    re.setCntCusDetailLine(bean.getInt("cntVal"));
                } else if ("11".equals(cntType)) {
                    re.setCntTotalPkg(bean.getInt("cntVal"));
                } else if ("51".equals(cntType)) {
                    re.setCntGoodsItem(bean.getInt("cntVal"));
                }
            }
        }


        return re;
    }
}
