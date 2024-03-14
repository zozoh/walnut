package com.site0.walnut.ext.data.tmpl.hdl;

import org.nutz.json.Json;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.tmpl.TmplContext;
import com.site0.walnut.ext.data.tmpl.TmplFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.tmpl.WnTmplTokenExpert;

public class tmpl_load extends TmplFilter {

    @Override
    protected void process(WnSystem sys, TmplContext fc, ZParams params) {

        // 从文件里获得
        if (params.hasString("f")) {
            String ph = params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            fc.tmpl = sys.io.readText(o);
        }
        // 从参数里获得
        else if (params.vals.length > 0) {
            fc.tmpl = params.val(0);
        }
        // 从管道获得
        else {
            fc.tmpl = sys.in.readAll();
        }

        // 生成模板符号专家类
        String by = params.getString("by");
        if (!Ws.isBlank(by)) {
            String[] ss = Json.fromJson(String[].class, by);
            if (ss.length != 3) {
                throw Er.create("e.cmd.tmpl.load.InvalidTokenExpert", by);
            }
            fc.expert = new WnTmplTokenExpert(ss[0], ss[1], ss[2].charAt(0));
        }
    }

}
