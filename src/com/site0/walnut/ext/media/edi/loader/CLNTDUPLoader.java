package com.site0.walnut.ext.media.edi.loader;


import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.clreg.IcsReplyCLNTDUP;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.util.Ws;
import org.nutz.lang.util.NutMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        re.setRefVer(rff.getInt("verionNum", 0));
        re.setFuncCode(rff.getInt("funcCode"));


        /**
         * 解析 refId, 就是 RFF+ABO 后面的字符串
         * RFF+ABO:M2E56QYLSZARUQCJSO::1'
         * */
        seg = finder.next("RFF", "ABO");
        if (null != seg) {
            rff.clear();
            seg.fillBean(rff, null, "type,rid,,refVer");
            if (rff.is("type", "ABO")) {
                re.setRefId(rff.getString("rid"));
                re.setRefVer(rff.getInt("refVer"));
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

            // 解析 重复的 Client 的 Name
            finder.reset();
            seg = finder.next("LOC", "ZZZ");
            if (seg != null) {
                rff.clear();
                seg.fillBean(rff, null, "code", ",,,name");
                re.setName(rff.getString("name"));
            }

            // 解析 重复的 Client 的 Address
            finder.reset();
            seg = finder.next("FTX", "BA");
            if (seg != null) {
                rff.clear();
                seg.fillBean(rff, null, "addrType", null, null, "addr1,addr2,locality,postcode,state", "country");
                re.setAddrType(rff.getString("addrType"));
                List<String> addrs = new ArrayList<>() {
                    {
                        add(rff.getString("addr1"));
                        add(rff.getString("addr2"));
                        add(rff.getString("locality"));
                        add(rff.getString("postcode"));
                        add(rff.getString("state"));
                        add(rff.getString("country"));
                    }
                };
                addrs.removeIf(Objects::isNull);
                re.setAddr(String.join(" ", addrs));
            }

            // 解析 重复的 Client 的 RoleNames
            finder.reset();
            List<EdiSegment> segList = finder.nextAll(false, "TAX", "RN");
            List<String> rns = new ArrayList<>();
            if (segList != null && segList.size() > 0) {
                for (EdiSegment item : segList) {
                    rff.clear();
                    item.fillBean(rff, null, null, ",,,rn");
                    if (!Ws.isBlank(rff.getString("rn"))) {
                        rns.add(rff.getString("rn"));
                    }
                }
                if (rns.size() > 0) {
                    re.setRoleNames(Ws.join(rns, ","));
                }
            }
        }
        return re;
    }
}
