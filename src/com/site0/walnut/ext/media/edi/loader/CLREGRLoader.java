package com.site0.walnut.ext.media.edi.loader;

import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.bean.EdiSegment;
import com.site0.walnut.ext.media.edi.msg.reply.EdiReplyError;
import com.site0.walnut.ext.media.edi.msg.reply.clreg.IcsReplyCLREGR;
import com.site0.walnut.ext.media.edi.util.EdiSegmentFinder;
import com.site0.walnut.ext.media.edi.util.IcsLoaderHelper;
import com.site0.walnut.util.Ws;
import org.nutz.lang.util.NutMap;

public class CLREGRLoader implements EdiMsgLoader<IcsReplyCLREGR> {
    @Override
    public Class<IcsReplyCLREGR> getResultType() {
        return IcsReplyCLREGR.class;
    }

    @Override
    public IcsReplyCLREGR load(EdiMessage msg) {
        IcsReplyCLREGR re = new IcsReplyCLREGR();
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
        // 11		Response
        re.setFuncCode(rff.getInt("funcCode"));

        /**
         * 若能找到 FTX+ABN 或者 FTX+CCID 报文行就算成功
         * FTX+ABN++15654834214'
         * FTX+CCI++AAA3436797Y'
         * */
        seg = finder.next("FTX", "^(ABN|CCI)$");
        if (null != seg) {
            rff.clear();
            seg.fillBean(rff, null, "subjectCode", null, "subjectValue");
            String reType = rff.getString("subjectCode");
            String reRid = rff.getString("subjectValue");
            if (!Ws.isBlank(reType) && !Ws.isBlank(reRid)) {
                re.setSuccess(true);
                re.setType(reType);
                re.setCode(reRid);
            }
        }
        /**
         * 解析 refId, 就是 RFF+ABO 后面的字符串
         * RFF+ABO:M2E4GJ7P2EJZI90354::001'
         * RFF+ABO:28558C74B757460991741B177754D008::001'
         * */
        finder.reset();
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
         * 解析 SG4: ERP-ERC-FTX 报文组, 收集错误信息
         *
         * ERP+1'
         * ERC+ADVICE:80:95'
         * ERC+MS5202:6:95'
         * FTX+AAO+++THIS TRANSACTION WAS ACCEPTED WITH ERRORS AND/OR WARNINGS'
         * ERP+1'
         * ERC+ADVICE:80:95'
         * ERC+CL0378:6:95'
         * FTX+AAO+++CCID =AAA3437644L CREATED SUCCESSFULLY'
         * */
        EdiReplyError[] errs = IcsLoaderHelper.parseERPErrs(finder);
        re.setErrs(errs);
        re.setErrCount(IcsLoaderHelper.errCount(errs));
        return re;
    }
}
