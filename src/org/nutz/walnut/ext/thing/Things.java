package org.nutz.walnut.ext.thing;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public abstract class Things {

    public static final int TH_LIVE = 1;
    public static final int TH_DEAD = -1;

    // 纯帮助函数集合
    private Things() {}

    /**
     * 找到一个对象所在的 Thing 对象
     * 
     * @param o
     *            参考对象
     * @return Thing 对象。 null 表示给出的对象不在一个 Thing 里
     */
    public static WnObj getThing(WnObj o) {
        // 自己就是
        if (o.isType("thing")) {
            return o;
        }

        // 找祖先
        while (o.hasParent()) {
            o = o.parent();
            if (o.isType("thing"))
                return o;
        }

        // 木找到
        return null;
    }

    /**
     * @see #getThing(WnObj)
     */
    public static WnObj checkThing(WnObj o) {
        WnObj oTx = getThing(o);
        if (null == oTx)
            throw Er.create("e.cmd.thing.notInThing", o);
        return oTx;
    }

    /**
     * 找到一个对象所在的 ThingSet
     * 
     * @param o
     *            参考对象
     * @return ThingSet。 null 表示给出的对象不在一个 ThingSet 里
     */
    public static WnObj getThingSet(WnObj o) {
        // 自己就是
        if (o.isType("thing_set")) {
            return o;
        }

        // 找祖先
        while (o.hasParent()) {
            o = o.parent();
            if (o.isType("thing_set"))
                return o;
        }

        // 木找到
        return null;
    }

    /**
     * @see #getThingSet(WnObj)
     */
    public static WnObj checkThingSet(WnObj o) {
        WnObj oTS = getThingSet(o);
        if (null == oTS)
            throw Er.create("e.cmd.thing.notInThingSet", o);
        return oTS;
    }

    /**
     * 找到一个对象所在的 ThingSet 或者 Thing 对象
     * 
     * @param o
     *            参考对象
     * @return ThingSet 或者 Thing 对象。 null 表示给出的对象不在一个 ThingSet 或者 Thing 对象里
     */
    public static WnObj getThingOrThingSet(WnObj o) {
        // 自己就是
        if (o.has("thing") || o.isType("thing")) {
            return o;
        }

        // 找祖先
        while (o.hasParent()) {
            o = o.parent();
            if (o.has("thing") || o.isType("thing"))
                return o;
        }

        // 木找到
        return null;
    }

    /**
     * @see #getThingOrThingSet(WnObj)
     */
    public static WnObj checkThingOrThingSet(WnObj o) {
        WnObj oTx = getThingOrThingSet(o);
        if (null == oTx)
            throw Er.create("e.cmd.thing.notInThingOrThingSet", o);
        return oTx;
    }

    /**
     * 根据参数填充元数据
     * 
     * @param sys
     *            系统接口
     * 
     * @param params
     *            参数表
     * 
     * @return 填充完毕的元数据
     */
    public static NutMap fillMeta(WnSystem sys, ZParams params) {
        // 得到所有字段
        String json = Cmds.getParamOrPipe(sys, params, "fields", false);
        NutMap meta = Lang.map(json);

        // 名称
        String th_nm = params.val(0);
        if (!Strings.isBlank(th_nm)) {
            meta.put("th_nm", th_nm);
        }

        // 摘要
        if (params.has("brief")) {
            meta.put("th_brief", params.get("brief"));
        }

        // 所有者
        if (params.has("ow")) {
            meta.put("th_ow", params.get("ow"));
        }

        // 分类
        if (params.has("cate")) {
            meta.put("th_cate", params.get("cate"));
        }

        // 返回传入的元数据
        return meta;
    }
}
