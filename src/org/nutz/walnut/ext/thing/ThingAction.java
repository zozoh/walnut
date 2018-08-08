package org.nutz.walnut.ext.thing;

import java.util.ArrayList;

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
                                    boolean required,
                                    boolean forUpdate) {
        if (null == ukeys || ukeys.length == 0)
            return null;

        // 准备查询条件
        WnQuery q = Wn.Q.pid(oIndex);
        q.setv("th_live", Things.TH_LIVE);

        // 排除
        if (null != oT)
            q.setv("id", Lang.map("$ne", oT.id()));

        // 逐个添加唯一键约束
        ArrayList<String> keyExists = new ArrayList<>(ukeys.length);
        ArrayList<String> keyNoNull = new ArrayList<>(ukeys.length);
        for (String key : ukeys) {
            Object val = meta.get(key);
            // 记录一下统计数据，后面再判断
            if (null == val) {
                if (meta.containsKey(key))
                    keyExists.add(key);
            }
            // 计入 NoNull
            else {
                keyExists.add(key);
                keyNoNull.add(key);
                // 记入查询条件
                q.setv(key, val);
            }
        }
        // 如果是复合键，尝试从原来对象里补偿一个
        if (null != oT
            && ukeys.length > 1
            && !keyExists.isEmpty()
            && keyExists.size() < ukeys.length) {
            for (String key : ukeys) {
                // 原来不存在的话，补偿一下
                if (!keyExists.contains(key)) {
                    Object val = oT.get(key);
                    if (null != val) {
                        keyExists.add(key);
                        keyNoNull.add(key);
                        q.setv(key, val);
                    }
                }
            }
        }

        // 归纳一下
        int nbNoNull = keyNoNull.size();
        int nbExists = keyExists.size();
        boolean allNoNull = nbNoNull == ukeys.length;
        boolean allExists = nbExists == ukeys.length;

        // 来判断一下，如果有键值不存在，那么分作两种情况
        // 如果是更新情况，则如果都没设置，也是可以过的
        // 否则如果是 required 的情况，那么就抛错
        // 还有一种情况，如果是复合键，只有部分键有值
        // 那么无论是不是 update 一定是不能容忍的，这个是最高优先级
        if (!allNoNull) {
            if (ukeys.length > 1 && nbExists > 0 && !allExists) {
                throw Er.create("e.thing.ukey.partly", Json.toJson(ukeys));
            }
            if (forUpdate) {
                if (nbExists == 0)
                    return null;
            }
            if (required) {
                throw Er.create("e.thing.ukey.required", Json.toJson(ukeys));
            }
        }

        // 查一下吧
        return io.getOne(q);
    }

    protected ThingUniqueKey checkDuplicated(WnObj oIndex,
                                             NutMap meta,
                                             WnObj oT,
                                             ThingUniqueKey[] uks,
                                             boolean forUpdate) {
        for (ThingUniqueKey tuk : uks) {
            WnObj oT2 = this.checkUniqueKeys(oIndex,
                                             oT,
                                             meta,
                                             tuk.getName(),
                                             tuk.isRequired(),
                                             forUpdate);
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
