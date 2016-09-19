package org.nutz.walnut.ext.thing;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public abstract class Things {

    public static final int TH_LIVE = 1;
    public static final int TH_DEAD = -1;

    /**
     * 检查一个对象是否是 Thing 的索引对象
     * 
     * @return 如果成功，返回该索引对象
     */
    public static WnObj checkThing(WnObj oT) {
        if (null == oT)
            throw Er.create("e.cmd.thing.null");
        if (oT.getInt("th_live", 0) != 0)
            throw Er.create("e.cmd.thing.notThing", oT);
        return oT;
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

    public static WnObj checkThingSetDir(WnIo io, WnObj oRefer, String dirName) {
        WnObj oTS = checkThingSet(oRefer);
        return io.check(oTS, dirName);
    }

    public static WnObj checkThingSetDir(WnSystem sys, WnObj oRefer, String dirName) {
        return checkThingSetDir(sys.io, oRefer, dirName);
    }

    public static WnObj checkThingDir(WnIo io, WnObj oRefer, String dirName) {
        WnObj oT = checkThing(oRefer);
        return io.check(oT, dirName);
    }

    public static WnObj checkThingDir(WnSystem sys, WnObj oRefer, String dirName) {
        return checkThingDir(sys.io, oRefer, dirName);
    }

    /**
     * 根据格式如 “TsID[/ThID]” 的字符串，得到 ThingSet 或者 Thing 对象
     * 
     * @param io
     *            IO 接口
     * @param str
     *            描述字符串，格式为 TsID[/ThID]
     * @return ThingSet 或者 Thing索引对象
     */
    public static WnObj checkRefer(WnIo io, String str) {
        String[] ss = Strings.splitIgnoreBlank(str, "/");
        // 指定了 TsID
        if (ss.length == 1) {
            return io.checkById(ss[0]);
        }
        // 指定了 TsID/ThId
        WnObj oTS = io.checkById(ss[0]);
        WnObj oTsIndexHome = checkThingSetDir(io, oTS, "index");
        return io.check(oTsIndexHome, ss[1]);
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
        NutMap meta = Strings.isBlank(json) ? new NutMap() : Lang.map(json);

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

    // ..........................................................
    // 纯帮助函数集合
    private Things() {}
}
