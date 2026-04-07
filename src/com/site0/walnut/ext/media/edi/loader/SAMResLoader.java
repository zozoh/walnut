package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.sam.IcsReplySamRes;
import com.site0.walnut.ext.media.edi.msg.reply.sam.SamAdv;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SAMResLoader implements EdiMsgLoader<IcsReplySamRes> {
    @Override
    public Class<IcsReplySamRes> getResultType() {
        return IcsReplySamRes.class;
    }

    @Override
    public IcsReplySamRes load(EdiMessage msg) {
        IcsReplySamRes re = new IcsReplySamRes();
        re.setSuccess(true);
        re.setRstVer(0);

        EdiSegmentFinder finder = msg.getFinder();
        NutMap bean = new NutMap();
        List<EdiSegment> segs;

        // BGM 报文行
        IcsLoaderHelper.parseBgmSeg(re, finder);

        // FTX 报文行
        boolean find = finder.moveToUtil("FTX", true, "NAD", "RFF", "DOC");
        if (find) {
            segs = finder.nextAll(true, "FTX");
            for (EdiSegment seg : segs) {
                bean.clear();
                seg.fillBean(bean, null, "subjectCode", null, null, "statusType,statusDesc");
                if ("AHN".equals(bean.getString("subjectCode"))) {
//                    re.setStatusType(bean.getString("statusType"));
//                    re.setStatusDesc(bean.getString("statusDesc"));
                    String stType = bean.getString("statusType");
                    String stDesc = bean.getString("statusDesc");
                    stType = stType != null ? stType.trim() : null;
                    stDesc = stDesc != null ? stDesc.trim() : null;
                    if (re.getImdStatus() == null) {
                        re.setImdStatus(new LinkedHashMap<>());
                    }
                    re.getImdStatus().put(stType, stDesc);
                }
            }
        }

        // NAD 报文行
        finder.reset();
        find = finder.moveToUtil("NAD", true, "RFF", "DOC");
        if (find) {
            segs = finder.nextAll(true, "NAD");
            for (EdiSegment seg : segs) {
                bean.clear();
                seg.fillBean(bean, null, "funcCode", "partyId,,agencyCode", "name1,name2,boxNum");
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

        // 解析 RFF 报文行
        finder.reset();
        find = finder.moveToUtil("RFF", true, "DOC");
        if (find) {
            segs = finder.nextAll(true, "RFF");
            for (EdiSegment item : segs) {
                bean.clear();
                item.fillBean(bean, null, "refCode,refVal,,refVer");
                String refCode = bean.getString("refCode");
                String refVal = bean.getString("refVal");
                if ("ABO".equals(refCode)) {
                    if (Strings.isNotBlank(refVal)) {
                        re.setRefId(refVal);
                        re.setRefIdInLower(refVal.toLowerCase());
                        re.setRefVer(bean.getInt("refVer"));
                    }
                } else if ("ABQ".equals(refCode)) {
                    re.setImporterRef(refVal);
                } else if ("ABT".equals(refCode)) {
                    re.setImpDecNum(refVal);
                    re.setImpDecVer(bean.getString("refVer"));
                } else if ("ADU".equals(refCode)) {
                    re.setBrokerRef(refVal);
                }
            }
        }

        // 解析 ERP-ERC-FTX 报文组
        finder.reset();
        find = finder.moveTo("DOC", false);
        while (find) {
            // 找到 ERP-ERC-FTX 报文组
            List<EdiSegment> errs = finder.nextAllUntilStopTag(true, new String[]{"ERP", "ERC", "FTX"}, new String[]{"DOC", "UNT"});

            if (!errs.isEmpty()) {
                SamAdv samAdv = new SamAdv();

                for (EdiSegment segment : errs) {
                    if (segment.isOf("ERP")) {
                        // example: ERP+::1'
                        bean.clear();
                        segment.fillBean(bean, null, ",,advId");
                        samAdv.setAdvId(bean.getString("advId"));
                    } else if (segment.isOf("ERC")) {
                        // example: ERC+1::95'
                        bean.clear();
                        segment.fillBean(bean, null, "advLoc,,agencyCode");
                        if (samAdv.getLocList() == null) {
                            samAdv.setLocList(new ArrayList<>());
                        }
                        samAdv.getLocList().add(bean.getString("advLoc"));
                    } else if (segment.isOf("FTX")) {
                        bean.clear();
                        segment.fillBean(bean, null, "subjectCode", null, null, "advDesc");
                        if ("ABS".equals(bean.getString("subjectCode"))) {
                            samAdv.setAdvDesc(bean.getString("advDesc"));
                        }
                    }
                }
                if (re.getAdvs() == null) {
                    re.setAdvs(new ArrayList<>());
                }
                re.getAdvs().add(samAdv);
            }
            find = finder.moveTo("DOC", false);
        }
        return re;
    }


}
