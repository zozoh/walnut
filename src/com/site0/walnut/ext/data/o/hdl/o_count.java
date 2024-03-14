package com.site0.walnut.ext.data.o.hdl;

import java.util.Collection;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnPager;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class o_count extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(mine)$");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        WnQuery q = new WnQuery();

        // 设置了条件
        if (params.vals.length > 0) {
            for (String val : params.vals) {
                NutMap map = Lang.map(val);
                q.add(map);
            }
        }
        // 试图从标准输入读取
        else {
            String json = sys.in.readAll();
            if (!Ws.isBlank(json)) {
                Object in = Json.fromJson(json);
                if (in instanceof Map<?, ?>) {
                    NutMap map = NutMap.WRAP((Map<String, Object>) in);
                    q.setAll(map);
                }
                // 集合
                else if (in instanceof Collection<?>) {
                    Collection<Map<String, Object>> col = (Collection<Map<String, Object>>) in;
                    for (Map<String, Object> it : col) {
                        NutMap map = NutMap.WRAP(it);
                        q.add(map);
                    }
                }
            }
        }

        // 设置父 ID
        WnObj oP = null;
        String pph = params.getString("p");
        if (!Ws.isBlank(pph)) {
            // 没有父就无视
            if (params.is("quiet")) {
                oP = Wn.getObj(sys, pph);
                if (null == oP) {
                    return;
                }
            }
            // 强制检查
            else {
                oP = Wn.checkObj(sys, pph);
            }
        }
        // 尝试上下文第一个对象
        else if (!fc.list.isEmpty()) {
            oP = fc.list.get(0);
        }
        if (null != oP) {
            NutMap map = Lang.map("pid", oP.id());
            q.setAllToList(map);
        }

        // 确保是 mine
        if (params.is("mine")) {
            q.setv("d0", "home").setv("d1", sys.getMyGroup());
        }

        WnPager wp = new WnPager(params);

        // 是否要带翻页呢？
        if (wp.countPage) {
            fc.pager = wp;
        }

        // 设置分页
        wp.setupQuery(sys, q);

        // 统计数量
        long count = sys.io.count(q);

        // 加入上下文
        if (params.has("as")) {
            String key = params.getString("as", "count");
            fc.summary.put(key, count);
        }
        // 直接输出
        else {
            fc.quiet = true;
            sys.out.println(count);
        }
    }

}
