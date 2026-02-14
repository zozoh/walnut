package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.pay.RefundAdv;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.util.List;

public class REFACCLoader implements EdiMsgLoader<RefundAdv> {

    @Override
    public Class<RefundAdv> getResultType() {
        return RefundAdv.class;
    }

    @Override
    public RefundAdv load(EdiMessage msg) {
        RefundAdv re = new RefundAdv();
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
                seg.fillBean(nutMap, null, "funcCode", "partyId,,agencyCode", "nameAddr");
                String funcCode = nutMap.getString("funcCode");
                String partyId = nutMap.getString("partyId");
                if ("CB".equals(funcCode)) {
                    re.setBrokerLicNum(partyId);
                    re.setBrokerName(nutMap.getString("nameAddr"));
                } else if ("MR".equals(funcCode)) {
                    re.setMsgRecipient(partyId);
                } else if ("VT".equals(funcCode)) {
                    re.setBranchId(partyId);
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
                        re.setRefId(refVal);
                        re.setRefIdInLower(refVal.toLowerCase());
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
