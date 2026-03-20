package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.pay.RefundAcc;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.util.List;

public class REFACCLoader implements EdiMsgLoader<RefundAcc> {

    @Override
    public Class<RefundAcc> getResultType() {
        return RefundAcc.class;
    }

    @Override
    public RefundAcc load(EdiMessage msg) {
        RefundAcc re = new RefundAcc();
        re.setRstVer(0);
        re.setSuccess(true);

        EdiSegmentFinder finder = msg.getFinder();
        NutMap nutMap = new NutMap();
        List<EdiSegment> segs;

        // BGM -> FuncCode
        IcsLoaderHelper.fillResFuncCode(re, finder);

        // FTX -> Sender/Client Ref
        // FTX+ACB+++ISMEL4106'
        boolean find = finder.moveToUtil("FTX", true, "UNT");
        if (find) {
            segs = finder.nextAll(true, "FTX");
            for (EdiSegment seg : segs) {
                nutMap.clear();
                // 1:SubjectCode, 2:TextFunc, 3:TextRef, 4:textValue
                seg.fillBean(nutMap, null, "subjectCode", null, null, "textValue");
                String subjectCode = nutMap.getString("subjectCode");
                String textValue = nutMap.getString("textValue");
                if ("ACB".equals(subjectCode)) {
                    re.setDrawbackId(textValue);
                } else if ("ACD".equals(subjectCode)) {
                    re.setCusActReason(textValue);
                }
            }
        }

        // NAD -> Parties
        finder.reset();
        find = finder.moveToUtil("NAD", true, "UNT");
        if (find) {
            segs = finder.nextAll(true, "NAD");
            for (EdiSegment seg : segs) {
                nutMap.clear();
                // NAD+CB++MASTER LOGISTICS PTY LTD'
                // 1st: FuncCode
                // 2nd: PartyId (C082)
                // 3rd: NameAndAddr (C058) -> Used for 'partyName' here for simple strings
                seg.fillBean(nutMap, null, "funcCode", "partyId,,agencyCode", "nameAddr1,nameAddr2,nameAddr3");
                String funcCode = nutMap.getString("funcCode");
                String partyId = nutMap.getString("partyId");
                if ("CB".equals(funcCode)) {
                    re.setBrokerLicNum(partyId);

                    // 将 nameAddr1/2 拼接成一个字符串，存储到 brokerName 中
                    String nameAddr1 = nutMap.getString("nameAddr1");
                    String nameAddr2 = nutMap.getString("nameAddr2");
                    if (nameAddr1 == null || nameAddr1.length() == 0) {
                        nameAddr1 = "";
                    }
                    if (nameAddr2 == null || nameAddr2.length() == 0) {
                        nameAddr2 = "";
                    }
                    String fullNameAddr = (nameAddr1 + nameAddr2).trim();
                    if (Strings.isNotBlank(fullNameAddr)) {
                        re.setBrokerName(fullNameAddr);
                    }

                    String nameAddr3 = nutMap.getString("nameAddr3");
                    if (nameAddr3 != null && nameAddr3.length() > 0) {
                        if (Strings.isNotBlank(nameAddr3)) {
                            re.setBrokerBoxNum(nameAddr3);
                        }
                    }
                } else if ("CC".equals(funcCode)) {
                    String nameAddr1 = nutMap.getString("nameAddr1");
                    String nameAddr2 = nutMap.getString("nameAddr2");
                    if (nameAddr1 == null || nameAddr1.length() == 0) {
                        nameAddr1 = "";
                    }
                    if (nameAddr2 == null || nameAddr2.length() == 0) {
                        nameAddr2 = "";
                    }
                    String fullNameAddr = (nameAddr1 + nameAddr2).trim();
                    if (Strings.isNotBlank(fullNameAddr)) {
                        re.setClientName(fullNameAddr);
                    }
                } else if ("CM".equals(funcCode)) {
                    String nameAddr1 = nutMap.getString("nameAddr1");
                    String nameAddr2 = nutMap.getString("nameAddr2");
                    String nameAddr3 = nutMap.getString("nameAddr3");
                    if (nameAddr1 != null && nameAddr1.length() > 0 && Strings.isNotBlank(nameAddr1)) {
                        re.setCusState(nameAddr1);
                    }
                    if (nameAddr2 != null && nameAddr2.length() > 0 && Strings.isNotBlank(nameAddr2)) {
                        re.setCusGroup(nameAddr2);
                    }
                    if (nameAddr3 != null && nameAddr3.length() > 0 && Strings.isNotBlank(nameAddr3)) {
                        re.setCusName(nameAddr3);
                    }
                } else if ("MR".equals(funcCode)) {
                    re.setMsgRecipient(partyId);
                } else if ("VT".equals(funcCode)) {
                    re.setBranchId(partyId);
                }
            }
        }

        // COM -> Contact
        finder.reset();
        find = finder.moveToUtil("COM", true, "UNT");
        if (find) {
            /*
            * Segment:	COM Communication Contact
                Position:	0130
                Group:	Segment Group 2 (Contact Information) Conditional (Optional)
                Level:	3
                Usage:	Conditional (Optional)
                Max Use:	9
                >Data Element Summary
                Data
                Element	Component
                Element	Name	Attributes
                M	C076		COMMUNICATION CONTACT	M	1
                M		3148	Communication number	M		an..512
                Customs Officer Email Address
                Customs Officer Facsimile Number
                Customs Officer Telephone Number
                M		3155	Communication number code qualifier	M		an..3
                EM		Electronic mail
                FX		Telefax
                TE		Telephone
            * */
            segs = finder.nextAll(true, "COM");
            for (EdiSegment seg : segs) {
                nutMap.clear();
                seg.fillBean(nutMap, null, "contactNum,contactType");
                String contactNum = nutMap.getString("contactNum");
                String contactType = nutMap.getString("contactType");
                if ("EM".equals(contactType)) {
                    re.setCusEmail(contactNum);
                } else if ("FX".equals(contactType)) {
                    re.setCusFax(contactNum);
                } else if ("TE".equals(contactType)) {
                    re.setCusTel(contactNum);
                }
            }
        }

        // RFF -> References
        finder.reset();
        find = finder.moveToUtil("RFF", true, "UNT");
        if (find) {
            segs = finder.nextAll(true, "RFF");
            for (EdiSegment seg : segs) {
                nutMap.clear();
                // RFF+ABO:Val::Ver'
                seg.fillBean(nutMap, null, "refCode,refVal,,refVer");
                String refCode = nutMap.getString("refCode");
                String refVal = nutMap.getString("refVal");
                String refVer = nutMap.getString("refVer");

                if ("ABO".equals(refCode)) {
                    if (Strings.isNotBlank(refVal)) {
                        re.setRefId(refVal.trim());
                        re.setRefIdInLower(refVal.trim().toLowerCase());
                        re.setRefVer(nutMap.getInt("refVer"));
                    }
                } else if ("ABT".equals(refCode)) {
                    re.setImpDecNum(refVal);
                    re.setImpDecNumVer(refVer);
                } else if ("ADU".equals(refCode)) {
                    re.setBrokerRef(refVal);
                }
            }
        }

        return re;
    }
}
