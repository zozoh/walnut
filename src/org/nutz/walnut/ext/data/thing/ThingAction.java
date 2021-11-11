package org.nutz.walnut.ext.data.thing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.data.thing.util.ThOtherUpdating;
import org.nutz.walnut.ext.data.thing.util.ThQuery;
import org.nutz.walnut.ext.data.thing.util.ThingCommandProton;
import org.nutz.walnut.ext.data.thing.util.ThingConf;
import org.nutz.walnut.ext.data.thing.util.ThingLinkKey;
import org.nutz.walnut.ext.data.thing.util.ThingLinkKeyTarget;
import org.nutz.walnut.ext.data.thing.util.ThingUniqueKey;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.validate.WnMatch;

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

    public WnObj checkDirTsData() {
        return Things.dirTsData(io, oTs);
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
            // For insert and UK is required
            if (required) {
                throw Er.create("e.thing.ukey.required", Json.toJson(ukeys));
            }
            return null;
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
                                                      WnExecutable executor) {
        // 检查所有的元数据是否合法
        conf.validate(meta, true);

        // 查看链接字段
        List<ThOtherUpdating> others = null;
        if (conf.hasLinkKeys()) {
            others = new ArrayList<>(conf.getLinkKeyMetaCapSize());
            for (Map<String, ThingLinkKey> linkKeys : conf.getLinkKeyList()) {
                for (Map.Entry<String, ThingLinkKey> en : linkKeys.entrySet()) {
                    // 获取链接键
                    String key = en.getKey();
                    ThingLinkKey lnk = en.getValue();

                    // 更新的元数据不包括这个键，就无视
                    if (!meta.containsKey(key))
                        continue;

                    // 什么都没做的话，无视好了
                    if (lnk.isDoNothing())
                        continue;

                    // 看看是否匹配上条件，如果匹配不上，也无视
                    if (!lnk.matchTestUpdate(meta)) {
                        continue;
                    }

                    if (!lnk.matchTestPrimary(oT)) {
                        continue;
                    }

                    // 准备 val 上下文
                    Object val = meta.get(key);
                    NutMap valContext = new NutMap();
                    valContext.putAll(oT);
                    valContext.putAll(meta);
                    valContext.put("@val", val);

                    // 看看值是否能匹配上
                    if (lnk.hasMatch()) {
                        WnMatch wm = lnk.getMatchObj();
                        if (!wm.match(val)) {
                            // 严格模式，抛错
                            if (lnk.isStrict()) {
                                throw Er.createf("e.cmd.thing.lnKey.NoMatch",
                                                 "Key:[%s], Val:[%s]",
                                                 key,
                                                 val);
                            }
                            continue;
                        }

                        Pattern p = lnk.getMatchPattern();
                        if (null != p) {
                            Matcher m = p.matcher(val.toString());
                            // 匹配的话填充 val 上下文
                            if (m.find()) {
                                for (int i = 0; i <= m.groupCount(); i++) {
                                    valContext.put("@g" + i, m.group(i));
                                }
                            }
                        }
                    }

                    // 准备
                    ThOtherUpdating other = new ThOtherUpdating(io, executor);

                    // 指定目标
                    if (lnk.hasTarget()) {
                        ThingLinkKeyTarget lnkTa = lnk.getTarget();
                        lnkTa = lnkTa.clone();
                        lnkTa.explain(valContext);
                        // 准备服务类【因为要更新其他ThingSet】
                        if (lnkTa.hasThingSet()) {
                            String tsph = lnkTa.getThingSet();
                            String tsaph = service.normalizeFullPath(tsph);
                            WnObj oTsOther = io.check(null, tsaph);
                            oTsOther = Things.checkThingSet(oTsOther);
                            other.service = service.gen(oTsOther, false);
                        }
                        // 直接更新的是一个目标
                        if (lnkTa.hasId()) {
                            String id = lnkTa.getId();
                            WnObj oTa = this.io.get(id);
                            if (null != oTa) {
                                other.list.add(oTa);
                            }
                        }
                        // 否则就是自身的数据集
                        else {
                            other.service = service;
                            // 在数据集中寻找更新目标
                            // 直接指定了 ID
                            if (lnkTa.hasId()) {
                                String id = lnkTa.getId();
                                WnObj oTa = other.service.getThing(id, false);
                                other.list.add(oTa);
                            }
                            // 寻找对应的记录集合
                            if (lnkTa.hasFilter()) {
                                NutMap fltTmpl = lnkTa.getFilter();
                                NutMap flt = new NutMap();
                                other.fillMeta(flt, fltTmpl, oT);
                                ThQuery tq2 = new ThQuery(flt);
                                other.list = other.service.queryList(tq2);
                            }
                        }
                    }
                    // 如果没有特别目标，就更新自己，那么就直接修改自己的 meta，不用记录到 others 里了
                    else if (lnk.hasSet()) {
                        other.fillMeta(meta, lnk.getSet(), valContext);
                        continue;
                    }
                    // 如果指定了特别运行的脚本
                    else if (lnk.hasRun()) {
                        // 生成脚本模板上下文
                        List<ThingCommandProton> protons = new ArrayList<>(lnk.getRun().length);
                        for (String cmdTmpl : lnk.getRun()) {
                            protons.add(new ThingCommandProton(oT, valContext, cmdTmpl));
                        }

                        // 计入，等 oT 更新后，这个会执行
                        // 这个采用了一个 Proton，是因为想用 oT 更新后的上下文
                        // 作为命令的模板的上下文
                        other.commands = protons;
                    }
                    // 啥都木有，那么过
                    else {
                        continue;
                    }

                    // 准备要更新的元数据表
                    if (lnk.hasSet()) {
                        other.meta = new NutMap();
                        other.fillMeta(other.meta, lnk.getSet(), valContext);
                    }

                    // 计入列表
                    if (!other.isIdle()) {
                        others.add(other);
                    }
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
