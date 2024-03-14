package com.site0.walnut.ext.media.ooml.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.ext.media.ooml.OomlContext;
import com.site0.walnut.ext.media.ooml.OomlFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.ooml.OomlEntry;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

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
