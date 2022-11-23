package org.nutz.walnut.ext.util.jsonx.hdl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.util.jsonx.JsonXContext;
import org.nutz.walnut.ext.util.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.each.WnEachIteratee;

public class jsonx_tmpl extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        List<Tmpl> tmpls = new ArrayList<>(params.vals.length);
        for (String str : params.vals) {
            Tmpl tmpl = Tmpl.parse(str);
            tmpls.add(tmpl);
        }

        // 渲染
        if (!tmpls.isEmpty()) {
            Wlang.each(fc.obj, new WnEachIteratee<Object>() {
                @SuppressWarnings("unchecked")
                public void invoke(int index, Object ele, Object src) {
                    // 准备上下文
                    NutMap context;
                    if (ele instanceof Map<?, ?>) {
                        context = NutMap.WRAP((Map<String, Object>) ele);
                    } else {
                        context = new NutMap();
                    }

                    // 准循环输出模板
                    StringBuilder sb = new StringBuilder();
                    Iterator<Tmpl> it = tmpls.iterator();

                    // 第一个节点
                    Tmpl tmpl = it.next();
                    sb.append(tmpl.render(context));

                    // 后续节点
                    while (it.hasNext()) {
                        tmpl = it.next();
                        sb.append(' ').append(tmpl.render(context));
                    }

                    // 输出
                    sys.out.println(sb.toString());
                }
            });
        }

        // 禁止主命令输出
        fc.quite = true;

    }

}
