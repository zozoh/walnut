package org.nutz.walnut.ext.util.strx.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.walnut.ext.util.strx.StrXContext;
import org.nutz.walnut.ext.util.strx.StrXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.each.WnEachIteratee;

public class strx_join extends StrXFilter {

    @Override
    protected void process(WnSystem sys, StrXContext fc, ZParams params) {
        String sep = Ws.sBlank(params.val(0), "");

        // 首先拼装
        List<Object> list = new LinkedList<>();

        Object val = Json.fromJson(fc.data);
        Wlang.each(val, new WnEachIteratee<Object>() {
            public void invoke(int index, Object ele, Object src) {
                list.add(ele);
            }
        });

        // 拼装
        fc.data = Ws.join(list, sep);
    }

}
