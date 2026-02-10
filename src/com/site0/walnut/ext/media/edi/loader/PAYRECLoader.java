package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.pay.PayRecRes;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PAYRECLoader implements EdiMsgLoader<PayRecRes> {
    @Override
    public Class<PayRecRes> getResultType() {
        return PayRecRes.class;
    }

    @Override
    public PayRecRes load(EdiMessage msg) {
        PayRecRes re = new PayRecRes();
        // 本次解析结果版本号，本版本为 0
        re.setRstVer(0);

        EdiSegmentFinder finder = msg.getFinder();
        NutMap nutMap = new NutMap();
        List<EdiSegment> segs;

        // 定位到 BGM 报文行，解析 FuncCode
        IcsLoaderHelper.fillResFuncCode(re, finder);

        // 定位到 DTM 报文行
        boolean find = finder.moveToUtil("DTM", true, "UNT");
        if (find) {
            // DTM+138:20221219:102'
            segs = finder.nextAll(true, "DTM");
            for (EdiSegment seg : segs) {
                nutMap.clear();
                seg.fillBean(nutMap, null, "funcCode,paymentDate,formatCode");
                if ("138".equals(nutMap.getString("funcCode"))) {
                    re.setPaymentDate(nutMap.getString("paymentDate"));
                }
            }
        }

        // 定位到 NAD 报文行，解析相关信息, NAD+MR+FJM396H::95'
        finder.reset();
        find = finder.moveToUtil("NAD", true, "UNT");
        if (find) {
            segs = finder.nextAll(true, "NAD");
            for (EdiSegment seg : segs) {
                nutMap.clear();
                seg.fillBean(nutMap, null, "funcCode", "partyId,,agencyCode,bankAccName");
                String funcCode = nutMap.getString("funcCode");
                String partyId = nutMap.getString("partyId");

                if ("AO".equals(funcCode)) {
                    // NAD+AO+{BankAccountNumber}::{215}++{BankAccName}'
                    re.setBankAccNum(partyId);
                    re.setBankAccName(nutMap.getString("bankAccName"));
                } else if ("CM".equals(funcCode)) {
                    re.setAbn(partyId);
                } else if ("COQ".equals(funcCode)) {
                    re.setBsbNum(partyId);
                } else if ("IM".equals(funcCode)) {
                    re.setImporterId(funcCode);
                } else if ("MR".equals(funcCode)) {
                    re.setMsgRecipient(partyId);
                } else if ("VT".equals(funcCode)) {
                    re.setBranchId(funcCode);
                }
            }
        }

        // 定位到 RFF 报文行
        finder.reset();
        find = finder.moveToUtil("RFF", true, "UNT");
        if (find) {
            segs = finder.nextAll(true, "RFF");
            for (EdiSegment seg : segs) {
                nutMap.clear();
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
                } else if ("ABQ".equals(refCode)) {
                    re.setImporterRef(refVal);
                } else if ("ABT".equals(refCode)) {
                    re.setImpDecNum(refVal);
                } else if ("ADU".equals(refCode)) {
                    re.setBrokerRef(refVal);
                } else if ("AII".equals(refCode)) {
                    re.setBankTransNum(refVal);
                } else if ("RA".equals(refCode)) {
                    re.setBankReceiptNum(refVal);
                }
            }
        }

        // 定位到 TAX 报文行，解析税金金额
        finder.reset();
        find = finder.moveToUtil("TAX", true, "UNT");
        if (find) {
            List<Map<String, String>> moaList = new ArrayList<>();
            segs = finder.nextAllUntilStopTag(true, new String[]{"TAX", "MOA"}, new String[]{"UNT"});
            for (EdiSegment seg : segs) {
                nutMap.clear();
                seg.fillBean(nutMap, null, "amountType,amountValue");
                String amountType = nutMap.getString("amountType");
                String amountValue = nutMap.getString("amountValue");
                if (amountValue != null && !amountValue.trim().isEmpty()) {
                    amountValue = new BigDecimal(amountValue).stripTrailingZeros().toPlainString();
                }
                Map<String, String> map = new HashMap<>();
                map.put("amtCode", amountType);
                map.put("amtValue", amountValue);
                moaList.add(map);
            }
            re.setMoaList(moaList);
        }
        return re;
    }
}
