package com.site0.walnut.ext.media.edi.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.ext.media.edi.EdiContext;
import com.site0.walnut.ext.media.edi.EdiFilter;
import com.site0.walnut.ext.media.edi.bean.EdiMessage;
import com.site0.walnut.ext.media.edi.loader.EdiMsgs;
import com.site0.walnut.ext.media.edi.loader.EdiMsgLoader;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

public class edi_msg extends EdiFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnl");
    }

    @Override
    protected void process(WnSystem sys, EdiContext fc, ZParams params) {
        fc.assertIC();

        String type = params.val(0, "AUTO");
        boolean keepList = params.is("l");
        boolean autoType = "AUTO".equalsIgnoreCase(type);

        // 逐个解析消息
        List<Object> objs = new ArrayList<>(fc.ic.getMessageCount());
        for (EdiMessage msg : fc.ic.getMessages()) {
            if(autoType) {
                type = EdiMsgs.getLoaderType(msg);
            }
            EdiMsgLoader<?> loader = EdiMsgs.checkLoader(type);
            Object obj = loader.load(msg);
            objs.add(obj);
        }

        Object output = objs;
        if (!keepList && objs.size() == 1) {
            output = objs.get(0);
        }

        JsonFormat jfmt = Cmds.gen_json_format(params);
        String json = Json.toJson(output, jfmt);
        sys.out.println(json);
    }

}
