package org.nutz.walnut.ext.data.o.hdl;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;
import org.nutz.walnut.util.validate.impl.MapMatch;
import org.nutz.walnut.util.validate.impl.ParallelMatch;

public class o_children extends OFilter {

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
        WnQuery q = new WnQuery();
        if (null != ing.query && ing.query.size() > 0) {
            q.setAllToList(ing.query);
        }
        q.setvToList("pid", o.id());
        q.limit(ing.limit);
        if (null != ing.sort) {
            q.sort(ing.sort);
        }
        List<WnObj> children = ing.sys.io.query(q);

        // 融合
        // ArrayList<WnObj> list = new ArrayList<>(children.size());
        ListIterator<WnObj> it = children.listIterator();
        while (it.hasNext()) {
            WnObj obj = it.next();
            if (obj.isHidden() && !ing.showHidden) {
                it.remove();
                continue;
            }
            // 加载 -axis 模式子节点们，已经读取过了，这里就是简单设置一下即可
            WnObj o2 = map.get(obj.id());
            if (null != o2) {
                it.set(o2);
            }
            // 非 -axis 模式，之前应该没有读过，现在再读一下
            else if (!ing.axis) {
                loadChildren(ing, obj, depth);
            }
        }

        if (null != children && !children.isEmpty()) {
            o.put(ing.childBy, children);
        }

    }

    static class Loading {
        WnSystem sys;
        WnMatch test;
        String childBy;
        NutMap query;
        NutMap sort;
        int limit;
        Map<String, WnObj> leafObjs;
        int depth;
        int force;
        boolean axis;
        boolean leaf;
        boolean showHidden;

        void joinLeafs(WnObj o) {
            List<WnObj> myChildren = o.getAsList(this.childBy, WnObj.class);
            if (null == myChildren) {
                leafObjs.put(o.id(), o);
            } else {
                for (WnObj child : myChildren) {
                    this.joinLeafs(child);
                }
            }
        }

        boolean canLoad(WnObj o, int depth) {
            if (null == o || !o.isDIR())
                return false;

            if (o.isHidden() && !this.showHidden)
                return false;

            if (this.depth > 0 && depth > this.depth) {
                return false;
            }

            if (!leaf && leafObjs.containsKey(o.id())) {
                return false;
            }

            if (depth < this.force) {
                return true;
            }

            return test.match(o);
        }
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(ignore|hidden|axis|leaf)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 有时候（譬如拼装 API指令时，支持一个 -ignore 比较方便
        if (params.is("ignore"))
            return;

        // 分析参数
        Loading ing = new Loading();
        ing.sys = sys;
        ing.query = params.getMap("query");
        ing.childBy = params.getString("by", "children");
        ing.force = params.getInt("force", 0);
        ing.depth = params.getInt("depth", 0);
        ing.sort = params.getMap("sort");
        ing.limit = params.getInt("limit", 1000);
        ing.showHidden = params.is("hidden", false);
        ing.axis = params.is("axis", false);
        ing.leaf = params.is("leaf", false);

        // 上下文节点，编制一个表，以备
        ing.leafObjs = new HashMap<>();
        for (WnObj o : fc.list) {
            ing.joinLeafs(o);
        }

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
