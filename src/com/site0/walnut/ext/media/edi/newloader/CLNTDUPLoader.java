package com.site0.walnut.ext.media.edi.newloader;


import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.loader.EdiMsgLoader;
import com.site0.walnut.ext.media.edi.newmsg.clreg.IcsReplyCLNTDUP;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.util.Ws;
import org.nutz.lang.util.NutMap;

import java.util.List;

public class CLNTDUPLoader implements EdiMsgLoader<IcsReplyCLNTDUP> {

    @Override
    public Class<IcsReplyCLNTDUP> getResultType() {
        return IcsReplyCLNTDUP.class;
    }

    @Override
    public IcsReplyCLNTDUP load(EdiMessage msg) {
        IcsReplyCLNTDUP re = new IcsReplyCLNTDUP();
        EdiSegmentFinder finder = msg.getFinder();

        EdiSegment seg;
        NutMap rff = new NutMap();

        /**
         * 定位到 BGM 报文行
         * BGM+961:::CLREGR+36F4 EFFC F764:001+11'
         * */
        seg = finder.next("BGM");
        rff.clear();
        seg.fillBean(rff, null, null, ",verionNum", "funcCode");
        re.setRefVer(rff.getInt("verionNum"));
        re.setFuncCode(rff.getInt("funcCode"));


        /**
         * 解析 refId, 就是 RFF+ABO 后面的字符串
         * RFF+ABO:M2E56QYLSZARUQCJSO::1'
         * */
        seg = finder.next("RFF", "ABO");
        if (null != seg) {
            rff.clear();
            seg.fillBean(rff, null, "type,rid");
            if (rff.is("type", "ABO")) {
                String rid = rff.getString("rid");
                if (null != rid) {
                    re.setRefId(rid);
                    re.setRefIdInLower(rid.toLowerCase());
                }
            }
        }
        /**
         *
         * 若能找到 ABN 或者 CCID 就算成功
         * DOC+CCI:::AAA3437644L'
         * */
        seg = finder.next("DOC", "^(ABN|CCI)$");
        if (null != seg) {
            rff.clear();
            seg.fillBean(rff, null, "subjectCode,,,subjectValue");
            String reType = rff.getString("subjectCode");
            String reRid = rff.getString("subjectValue");
            if (!Ws.isBlank(reType) && !Ws.isBlank(reRid)) {
                re.setSuccess(true);
                re.setType(reType);
                re.setCode(reRid);
            }
        }

        /**
         * 解析 重复的 Client 的 Name , Address 和 RoleNames
         * LOC+ZZZ+:::XIAMEN NEW HAOXIN TRADING CO., LTD'
         * FTX+BA+++NO,604, 4-4 HAITIAN ROAD::XIAMEN:361000:FJ+CN'
         * TAX+RN+:::SUPPLIER'
         * */
        if (re.isSuccess()) {
            finder.reset();
            seg = finder.next("LOC", "ZZZ");
            if (seg != null) {
                rff.clear();
                seg.fillBean(rff, null, "code", ",,,name");
                re.setName(rff.getString("name"));
            }

            finder.reset();
            seg = finder.next("FTX", "BA");
            if (seg != null) {
                rff.clear();
                // seg.fillBean(rff, null, "addrType", null, null, "addr1,addr2,locality,postcode,state,country");
                seg.fillBean(rff, null, "addrType", null, null, "addr");
                re.setAddrType(rff.getString("addrType"));
                re.setAddr(rff.getString("addr"));
            }

            finder.reset();
            List<EdiSegment> segList = finder.nextAll(false, "TAX", "RN");
            String rns = null;
            if (segList != null && segList.size() > 0) {
                for (EdiSegment item : segList) {
                    rff.clear();
                    seg.fillBean(rff, null, null, ",,,rn");
                    rns += rff.getString("rn");
                }
            }
            re.setRoleNames(rns);
        }

        return re;
    }
}
