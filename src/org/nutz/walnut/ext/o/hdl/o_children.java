package org.nutz.walnut.ext.o.hdl;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.o.OContext;
import org.nutz.walnut.ext.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.validate.WnMatch;
import org.nutz.walnut.validate.impl.AutoMatch;
import org.nutz.walnut.validate.impl.MapMatch;
import org.nutz.walnut.validate.impl.ParallelMatch;

public class o_children extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(hidden)$");
    }

    private void loadChildren(Loading ing, WnObj o, int depth) {
        // 是否需要读取
        if (!ing.canLoad(o, depth))
            return;

        // 深入一层
        depth++;

        // 首先得到它所有现存的子
        List<WnObj> myChildren = o.getAsList(ing.childBy, WnObj.class);
        Map<String, WnObj> map = new HashMap<>();
        if (null != myChildren) {
            for (WnObj child : myChildren) {
                map.put(child.id(), child);

                // 递归读取
                loadChildren(ing, child, depth);
            }
        }

        // 读取子孙
        List<WnObj> children = ing.sys.io.getChildren(o, null);

        // 融合
        // ArrayList<WnObj> list = new ArrayList<>(children.size());
        ListIterator<WnObj> it = children.listIterator();
        while (it.hasNext()) {
            WnObj obj = it.next();
            if (obj.isHidden() && !ing.showHidden) {
                it.remove();
                continue;
            }
            WnObj o2 = map.get(obj.id());
            if (null != o2) {
                it.set(o2);
            }
        }

        o.put(ing.childBy, children);

    }

    static class Loading {
        WnSystem sys;
        WnMatch test;
        String childBy;
        int force;
        boolean showHidden;

        boolean canLoad(WnObj o, int depth) {
            if (null == o || !o.isDIR())
                return false;

            if (o.isHidden() && !this.showHidden)
                return false;

            if (depth < force)
                return true;
            return test.match(o);
        }
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 分析参数
        Loading ing = new Loading();
        ing.sys = sys;
        ing.childBy = params.getString("by", "children");
        ing.force = params.getInt("force", 0);
        ing.showHidden = params.is("hidden", false);

        // 准备过滤器
        WnMatch[] ms = new WnMatch[params.vals.length];
        int i = 0;
        for (String val : params.vals) {
            Object vo = Json.fromJson(val);
            WnMatch m = new AutoMatch(vo);
            ms[i++] = m;
        }
        if (ms.length == 0) {
            ing.test = new MapMatch(Lang.map("race", "DIR"));
        } else if (ms.length == 1) {
            ing.test = ms[0];
        } else {
            ing.test = new ParallelMatch(ms);
        }

        // 递归读取
        int depth = 0;
        for (WnObj o : fc.list) {
            loadChildren(ing, o, depth);
        }
    }

}
