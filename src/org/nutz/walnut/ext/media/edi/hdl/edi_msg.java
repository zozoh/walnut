package org.nutz.walnut.ext.media.edi.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.media.edi.EdiContext;
import org.nutz.walnut.ext.media.edi.EdiFilter;
import org.nutz.walnut.ext.media.edi.bean.EdiMessage;
import org.nutz.walnut.ext.media.edi.loader.EdiMsgs;
import org.nutz.walnut.ext.media.edi.loader.EdiMsgLoader;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class edi_msg extends EdiFilter {

    @Override
    protected void process(WnSystem sys, EdiContext fc, ZParams params) {
        fc.assertIC();

        String type = params.val_check(0);

        EdiMessage msg = fc.ic.getFirstMessage();
        EdiMsgLoader<?> loader = EdiMsgs.checkLoader(type);
        Object obj = loader.load(msg);

        JsonFormat jfmt = Cmds.gen_json_format(params);
        String json = Json.toJson(obj, jfmt);
        sys.out.println(json);
    }

}
