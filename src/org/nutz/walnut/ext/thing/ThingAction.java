package org.nutz.walnut.ext.thing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.thing.util.ThOtherUpdating;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.ext.thing.util.ThingConf;
import org.nutz.walnut.ext.thing.util.ThingLinkKey;
import org.nutz.walnut.ext.thing.util.ThingLinkKeyTarget;
import org.nutz.walnut.ext.thing.util.ThingUniqueKey;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.util.Wn;

public abstract class ThingAction<T> {

    protected WnThingService service;

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

    public WnThingService getService() {
        return service;
    }

    public void setService(WnThingService service) {
        this.service = service;
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public WnObj getoTs() {
        return oTs;
    }

    public void setoTs(WnObj oTs) {
        this.oTs = oTs;
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

    protected List<ThOtherUpdating> evalOtherUpdating(WnObj oT,
                                                      NutMap meta,
                                                      ThingConf conf,
                                                      boolean isCreate) {
        // 检查所有的元数据是否合法
        conf.validate(meta, true);

        // 查看链接字段
        List<ThOtherUpdating> others = null;
        if (conf.hasLinkKeys()) {
            others = new ArrayList<>(conf.getLinkKeys().size());
            for (Map.Entry<String, ThingLinkKey> en : conf.getLinkKeys().entrySet()) {
                String key = en.getKey();
                ThingLinkKey lnk = en.getValue();

                // 木有值的话，就忽略
                Object val = meta.get(key);
                if (null == val)
                    continue;

                // 确保有 set，没有设置值的话也木有意义
                if (!lnk.hasSet())
                    continue;

                // 准备 val 上下文
                NutMap valContext = new NutMap();
                valContext.put("val", val);

                // 看看值是否能匹配上
                if (lnk.hasMatch()) {
                    Matcher m = lnk.getMatch().matcher(val.toString());
                    // 严格模式，抛错
                    if (!m.find() && lnk.isStrict()) {
                        throw Er.createf("e.cmd.thing.lnKey.NoMatch",
                                         "Key:[%s], Val:[%s]",
                                         key,
                                         val);
                    }
                    // 填充 val 上下文
                    for (int i = 0; i <= m.groupCount(); i++) {
                        valContext.put("g" + i, m.group(i));
                    }
                }

                // 准备
                ThOtherUpdating other = new ThOtherUpdating();

                // 指定目标
                if (lnk.hasTarget()) {
                    ThingLinkKeyTarget lnkTa = lnk.getTarget();
                    // 准备服务类
                    if (lnkTa.hasThingSet()) {
                        String tsph = lnkTa.getThingSet();
                        String tsaph = service.normalizeFullPath(tsph);
                        WnObj oTsOther = io.check(null, tsaph);
                        oTsOther = Things.checkThingSet(oTsOther);
                        other.service = service.gen(oTsOther, false);
                    }

                    // 准备查询
                    ThQuery tq2;

                    // 寻找对应的记录集合
                    if (lnkTa.hasFilter()) {
                        NutMap fltTmpl = lnkTa.getFilter();
                        NutMap flt = new NutMap();
                        other.fillMeta(flt, fltTmpl, oT);
                        tq2 = new ThQuery(flt);
                    }
                    // 否则
                    else {
                        tq2 = new ThQuery();
                    }

                    // 查找要修改的目标
                    other.list = other.service.queryList(tq2);
                }
                // 如果没有就更新自己，那么就直接修改自己的 meta，不用记录到 others 里了
                else {
                    other.fillMeta(meta, lnk.getSet(), valContext);
                    continue;
                }

                // 准备要更新的元数据表
                other.meta = new NutMap();
                other.fillMeta(other.meta, lnk.getSet(), valContext);

                // 计入列表
                if (other.list.size() > 0 && null != other.meta && other.meta.size() > 0) {
                    others.add(other);
                }
            }
        }
        return others;
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
