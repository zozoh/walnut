package org.nutz.walnut.ext.ooml.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.ooml.OomlContext;
import org.nutz.walnut.ext.ooml.OomlFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class ooml_entry extends OomlFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "cqnbish", "^(list)$");
    }

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        List<OomlEntry> list = fc.ooml.getEntries();

        // 列表模式
        if (params.is("list")) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (OomlEntry li : list) {
                sb.append(i++).append('.');
                sb.append(li.toString());
                sb.append('\n');
            }
            sys.out.println(sb);
        }
        // 输出 BEAN
        else {
            List<NutBean> outs = new ArrayList<>(list.size());
            for (OomlEntry li : list) {
                outs.add(li.toBean());
            }
            // 表格模式
            if (params.has("t")) {
                if (Ws.isBlank(params.getString("t"))) {
                    params.setv("t", "race,type,size,content,path");
                }
                Cmds.output_objs_as_table(sys, params, null, outs);
            }
            // JSON 模式
            else {
                JsonFormat jfmt = Cmds.gen_json_format(params);
                String json = Json.toJson(outs, jfmt);
                sys.out.println(json);
            }
        }
    }
}
