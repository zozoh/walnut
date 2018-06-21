package org.nutz.walnut.ext.thing.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.ThingAction;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.util.Wn;

public class CreateThingAction extends ThingAction<List<WnObj>> {

    private String uniqueKey;

    private List<NutMap> metas;

    public CreateThingAction() {
        this.metas = new LinkedList<>();
    }

    public CreateThingAction setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
        return this;
    }

    public CreateThingAction addMeta(NutMap... metas) {
        for (NutMap meta : metas)
            this.metas.add(meta);
        return this;
    }

    public CreateThingAction addAllMeta(List<NutMap> metas) {
        this.metas.addAll(metas);
        return this;
    }

    @Override
    public List<WnObj> invoke() {
        // 找到索引
        WnObj oIndex = this.checkDirTsIndex();

        // 准备返回值
        List<WnObj> list = new ArrayList<>(metas.size());

        // 来吧，循环生成
        for (NutMap meta : metas) {
            WnObj oT = __create_one(oIndex, meta);
            list.add(oT);
        }

        // 返回
        return list;
    }

    private WnObj __create_one(WnObj oIndex, NutMap meta) {
        // 创建或者取得一个一个 Thing
        WnObj oT = null;

        // 看看如果声明了唯一键
        if (!Strings.isBlank(this.uniqueKey)) {
            Object uval = meta.get(this.uniqueKey);
            if (null != uval) {
                WnQuery q = Wn.Q.pid(oIndex);
                q.setv("th_live", Things.TH_LIVE);
                q.setv(this.uniqueKey, uval);
                oT = io.getOne(q);
            }
        }
        // 木有，那么就创建咯
        if (null == oT) {
            oT = io.create(oIndex, "${id}", WnRace.FILE);
        }

        // 设置更多的固有属性
        meta.put("th_set", oTs.id());
        meta.put("th_live", Things.TH_LIVE);

        // 默认的内容类型
        if (!meta.has("mime") && meta.has("tp"))
            meta.put("mime", io.mimes().getMime(meta.getString("tp"), "text/plain"));

        // zozoh: 不知道下面几行代码动机是啥，没有就不设置呗。靠，先注释掉
        // // 图标
        // if (!meta.has("icon") && !oT.has("icon"))
        // meta.put("icon", oTs.get("th_icon"));
        //
        // // 缩略图
        // if (!meta.has("thumb") && !oT.has("thumb") && oTs.has("th_thumb"))
        // meta.put("thumb", oTs.get("th_thumb"));

        // 更新这个 Thing
        io.appendMeta(oT, meta);

        // 返回
        return oT;
    }

}
