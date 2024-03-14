package com.site0.walnut.ext.util.strx.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import com.site0.walnut.ext.util.strx.StrXContext;
import com.site0.walnut.ext.util.strx.StrXFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.each.WnEachIteratee;

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
