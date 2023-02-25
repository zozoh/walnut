package org.nutz.walnut.ext.data.o.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Wobj;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.util.explain.WnExplain;
import org.nutz.walnut.util.explain.WnExplains;

public class WnObjJoin {

    private WnObjTrans trans;

    private WnSystem sys;

    private Tmpl parentPath;

    private boolean isFetch;

    private WnExplain query;

    private String toKey;

    private NutMap sort;

    private int limit;

    private int skip;

    private WnObj obj;

    private WnObj oP;

    public WnObjJoin(WnSystem sys) {
        this.sys = sys;
    }

    public void loadFrom(ZParams params) {
        // 解析父路径模板
        String pph = params.val(0);
        if (!Ws.isBlank(pph)) {
            this.parentPath = Tmpl.parse(pph);
        }
        // 解析查询模板
        String q = params.getString("query");
        Object qo = Wlang.anyToObj(q);
        query = WnExplains.parse(qo);

        // 读取参数
        isFetch = params.is("fetch");
        toKey = params.getString("to", "...");
        sort = params.getMap("sort");
        limit = params.getInt("limit", 100);
        skip = params.getInt("skip", 0);

        // 检查
        if (Ws.isBlank(toKey)) {
            throw Er.create("e.obj.join.NilTargetKey");
        }
        if (Wobj.isReserveKey(toKey)) {
            throw Er.create("e.obj.join.InvalidTargetKey", toKey);
        }
    }

    public WnObj loadObjAndParent(WnObj obj) {
        this.obj = obj;
        if (null != parentPath) {
            String pph = parentPath.render(obj);
            oP = Wn.checkObj(sys, pph);
        }
        return oP;
    }

    public List<WnObj> query() {
        Object qo = query.explain(obj);
        WnQuery q = Wn.Q.any(qo);
        // 仅仅采用指定路径
        if (q.isEmptyMatch() && null != oP && isFetch) {
            return Wlang.list(oP);
        }
        //
        // 还是要查询
        //
        // 指定父对象
        if (null != oP) {
            q.setvToList("pid", oP.id());
        }
        // 默认父对象
        else {
            q.setvToList("pid", obj.parentId());
        }
        q.sort(sort).limit(limit).skip(skip);

        return sys.io.query(q);
    }

    public List<Object> queryAndTrans() {
        List<WnObj> list = query();
        if (null != trans) {
            return trans.translate(list);
        }
        List<Object> re = new ArrayList<>();
        re.addAll(list);
        return re;
    }

    @SuppressWarnings("unchecked")
    public void joinToObj(Object val) {
        if (null != val) {
            // 解构
            if ("...".equals(toKey)) {
                if (val instanceof Map) {
                    NutMap vm = NutMap.WRAP((Map<String, Object>) val);
                    for (Map.Entry<String, Object> en : vm.entrySet()) {
                        String k = en.getKey();
                        Object v = en.getValue();
                        if (!Wobj.isReserveKey(k)) {
                            obj.put(k, v);
                        }
                    }
                }
                // 还是设置吧
                else {
                    obj.put(toKey, val);
                }
            }
            // 直接设置
            else if (!Wobj.isReserveKey(toKey)) {
                obj.put(toKey, val);
            }
        }
    }

    public WnObjJoin clone() {
        WnObjJoin join = new WnObjJoin(sys);
        join.parentPath = this.parentPath;
        join.query = this.query;
        join.isFetch = this.isFetch;
        join.toKey = this.toKey;
        join.sort = this.sort;
        join.limit = this.limit;
        join.skip = this.skip;
        join.trans = this.trans;
        return join;
    }

    public WnObjTrans getTrans() {
        return trans;
    }

    public void setTrans(WnObjTrans trans) {
        this.trans = trans;
    }

    public WnSystem getSys() {
        return sys;
    }

    public void setSys(WnSystem sys) {
        this.sys = sys;
    }

    public WnExplain getQuery() {
        return query;
    }

    public void setQuery(WnExplain query) {
        this.query = query;
    }

    public String getToKey() {
        return toKey;
    }

    public void setToKey(String toKey) {
        this.toKey = toKey;
    }

    public NutMap getSort() {
        return sort;
    }

    public void setSort(NutMap sort) {
        this.sort = sort;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

}
