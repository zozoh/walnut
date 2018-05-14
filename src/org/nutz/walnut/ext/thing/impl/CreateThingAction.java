package org.nutz.walnut.ext.thing.impl;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.ThingAction;
import org.nutz.walnut.ext.thing.util.Things;

public class CreateThingAction extends ThingAction<WnObj> {

    private NutMap meta;

    private String th_nm;

    public CreateThingAction setMeta(NutMap meta) {
        this.meta = meta;
        return this;
    }

    public CreateThingAction setName(String th_nm) {
        this.th_nm = th_nm;
        return this;
    }

    @Override
    public WnObj invoke() {
        // 找到索引
        WnObj oIndex = this.checkDirTsIndex();

        // 创建一个 Thing
        WnObj oT = io.create(oIndex, "${id}", WnRace.FILE);

        // 名称
        if (!Strings.isBlank(th_nm)) {
            meta.put("th_nm", th_nm);
        }

        // 设置更多的固有属性
        meta.put("tp", "th_index");
        meta.put("th_set", oTs.id());
        meta.put("th_live", Things.TH_LIVE);

        // 默认的内容类型
        if (!meta.has("mime"))
            meta.put("mime", "text/plain");

        // 图标
        if (!meta.has("icon"))
            meta.put("icon", oTs.get("th_icon"));

        // 缩略图
        if (!meta.has("thumb"))
            meta.put("thumb", oTs.get("th_thumb"));

        // 更新这个 Thing
        io.appendMeta(oT, meta);

        // 返回
        return oT;
    }

}
