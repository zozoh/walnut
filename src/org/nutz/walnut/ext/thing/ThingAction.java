package org.nutz.walnut.ext.thing;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.thing.util.ThingUniqueKey;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.util.Wn;

public abstract class ThingAction<T> {

    protected WnIo io;

    protected WnObj oTs;

    // protected T output;

    public abstract T invoke();

    public WnObj checkThIndex(String id) {
        return Things.checkThIndex(io, oTs, id);
    }

    public WnObj getThIndex(String id) {
        return Things.getThIndex(io, oTs, id);
    }

    public WnObj checkDirTsIndex() {
        return Things.dirTsIndex(io, oTs);
    }

    public WnIo getIo() {
        return io;
    }

    public ThingAction<T> setIo(WnIo io) {
        this.io = io;
        return this;
    }

    public WnObj getThingSet() {
        return oTs;
    }

    public ThingAction<T> setThingSet(WnObj oTs) {
        this.oTs = oTs;
        return this;
    }

    /**
     * 根据唯一键约束，检查元数据是否符合条件
     * 
     * @param oIndex
     *            索引目录
     * @param oT
     *            当前数据对象。如果给定，则查找时排除这个数据
     * @param meta
     *            元数据
     * @param ukeys
     *            唯一键列表
     * @param required
     *            是否必须有值
     * @return 已经存在且匹配唯一键的数据
     */
    protected WnObj checkUniqueKeys(WnObj oIndex,
                                    WnObj oT,
                                    NutMap meta,
                                    String[] ukeys,
                                    boolean required) {
        if (null == ukeys || ukeys.length == 0)
            return null;

        // 准备查询条件
        WnQuery q = Wn.Q.pid(oIndex);
        q.setv("th_live", Things.TH_LIVE);

        // 排除
        if (null != oT)
            q.setv("id", Lang.map("$ne", oT.id()));

        // 逐个添加唯一键约束
        for (String key : ukeys) {
            Object val = meta.get(key);
            // 必须存在吗？
            if (null == val) {
                if (required) {
                    throw Er.create("e.thing.ukey.required", Json.toJson(ukeys));
                }
                continue;
            }
            // 来，查一下
            q.setv(key, val);
        }

        // 查一下吧
        return io.getOne(q);
    }

    protected ThingUniqueKey checkDuplicated(WnObj oIndex,
                                             NutMap meta,
                                             WnObj oT,
                                             ThingUniqueKey[] uks) {
        for (ThingUniqueKey tuk : uks) {
            WnObj oT2 = this.checkUniqueKeys(oIndex, oT, meta, tuk.getName(), tuk.isRequired());
            if (null != oT2) {
                if (null != oT && oT.isSameId(oT2))
                    continue;
                return tuk;
            }
        }
        return null;
    }

    // public T getOutput() {
    // return output;
    // }
    //
    // public ThingAction<T> setOutput(T output) {
    // this.output = output;
    // return this;
    // }

}
