package org.nutz.walnut.ext.hmaker.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(regex = "^(obj)$", value = "cqn")
public class hmaker_sites implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 得到要查找的域，默认的用自己的当前域
        String dmn = hc.params.getString("d", sys.se.group());

        // 准备查询条件
        WnQuery q = new WnQuery();
        q.setv("d1", dmn);
        q.setv("tp", "hmaker_site");
        q.setv("race", WnRace.DIR);

        // 执行查找
        List<WnObj> list = sys.io.query(q);

        // 指定输出字段
        if (hc.params.has("key")) {
            String[] keys = Strings.splitIgnoreBlank(hc.params.get("key"));

            // 如果只有一个字段，默认会输出字符串，除非强制为对象
            if (keys.length == 1 && !hc.params.is("obj")) {
                List<Object> outs = new ArrayList<>(list.size());
                String key = keys[0];
                for (WnObj o : list) {
                    Object v = o.get(key);
                    outs.add(v);
                }
                sys.out.print(Json.toJson(outs, hc.jfmt));
            }
            // 过滤字段
            else {
                List<NutMap> outs = new ArrayList<>(list.size());
                for (WnObj o : list) {
                    NutMap map = NutMap.WRAP(o).pick(keys);
                    outs.add(map);
                }
                sys.out.print(Json.toJson(outs, hc.jfmt));
            }
        }
        // 默认的输出全部的 JSON
        else {
            sys.out.print(Json.toJson(list, hc.jfmt));
        }

    }

}
