package org.nutz.walnut.ext.data.o.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wcol;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_tmpl extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 准备模板字符串
        String tmpl = Ws.join(params.vals, " ");
        Tmpl t = Tmpl.parse(tmpl);

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
