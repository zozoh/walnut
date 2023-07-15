package org.nutz.walnut.ext.media.edi.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.ext.media.edi.EdiContext;
import org.nutz.walnut.ext.media.edi.EdiFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class edi_view extends EdiFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqn");
    }

    @Override
    protected void process(WnSystem sys, EdiContext fc, ZParams params) {

        String as = params.getString("as", "json");

        // 输出上下文文本
        if ("msg".equals(as)) {
            fc.assertMessage();
            sys.out.println(fc.message);
            return;
        }

        // 下面的输出就需要有报文对象
        fc.assertIC();
        // 作为 tree 输出
        if ("tree".equals(as)) {
            String str = fc.ic.toTree();
            sys.out.println(str);
        }
        // 输出上下文文本
        else if ("text".equals(as)) {
            String str = fc.ic.toString();
            sys.out.println(str);
        }
        // 作为JSON 输出
        else {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String json = Json.toJson(fc.ic, jfmt);
            sys.out.println(json);
        }
    }

}
