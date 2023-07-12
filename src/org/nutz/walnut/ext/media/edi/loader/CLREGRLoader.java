package org.nutz.walnut.ext.media.edi.loader;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.media.edi.bean.EdiMessage;
import org.nutz.walnut.ext.media.edi.bean.EdiSegment;
import org.nutz.walnut.ext.media.edi.reply.EdiReplyCLREGR;

public class CLREGRLoader implements EdiMsgLoader<EdiReplyCLREGR> {

    @Override
    public EdiReplyCLREGR trans(EdiMessage en) {
        EdiReplyCLREGR re = new EdiReplyCLREGR();

        // 如果能找到 ABN 或者 CCID 就算成功
        for (EdiSegment seg : en.getSegments()) {
            // 指明消息的参考 ID
            if (seg.isTag("RFF")) {
                NutMap rff = new NutMap();
                seg.fillBean(rff, null, "type,rid");
                if (rff.is("type", "ABO")) {
                    String rid = rff.getString("rid");
                    if (null != rid) {
                        re.setReferId(rid);
                        continue;
                    }
                }
            }
            // 指明 ABN
            if(seg.isTag("FTX")) {
                
            }
        }

        // 收集错误信息
        return re;
    }

    @Override
    public Class<EdiReplyCLREGR> getResultType() {
        return EdiReplyCLREGR.class;
    }

}
