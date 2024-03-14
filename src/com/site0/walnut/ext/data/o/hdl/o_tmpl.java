package com.site0.walnut.ext.data.o.hdl;

import java.util.ArrayList;
import java.util.List;

import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wcol;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class o_tmpl extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 准备模板字符串
        String tmpl = Ws.join(params.vals, " ");
        WnTmpl t = WnTmpl.parse(tmpl);

        // 准备结果列表
        List<String> list = new ArrayList<>(fc.list.size());

        // 准备分隔符
        String sep = params.get("sep", "\n");
        sep = Ws.unescape(sep);

        // 依次渲染结果
        for (WnObj o : fc.list) {
            String str = t.render(o);
            list.add(str);
        }

        // 连接输出结果
        String out = Wcol.join(list, sep);
        sys.out.println(out);

        fc.quiet = true;
    }

}
