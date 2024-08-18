package com.site0.walnut.ext.media.edi.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.ext.media.edi.EdiContext;
import com.site0.walnut.ext.media.edi.EdiFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

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

        // 作为 tree 输出
        if ("tree".equals(as)) {
            fc.assertIC();
            String str = fc.ic.toTree();
            sys.out.println(str);
        }
        // 输出上下文文本
        else if ("text".equals(as)) {
            fc.assertIC();
            String str = fc.ic.toString();
            sys.out.println(str);
        }
        // 作为JSON 输出
        else if ("json".equals(as)) {
            fc.assertIC();
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String json = Json.toJson(fc.ic, jfmt);
            sys.out.println(json);
        }
        // 输出关联的变量
        else if ("vars".equals(as)) {
            if (!Ws.isBlank(fc.vars)) {
                Object vars = Json.fromJson(fc.vars);
                JsonFormat jfmt = Cmds.gen_json_format(params);
                String json = Json.toJson(vars, jfmt);
                sys.out.println(json);
            } else {
                sys.out.println("{}");
            }
        }
        // 默认输出原始报文对象
        else {
            sys.out.println(fc.raw_input);
        }
    }

}
