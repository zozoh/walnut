package org.nutz.walnut.ext.thing.impl;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.ThingAction;
import org.nutz.walnut.ext.thing.util.Things;

public class UpdateThingAction extends ThingAction<WnObj> {

    private String id;

    private NutMap meta;

    private String th_nm;

    public UpdateThingAction setId(String id) {
        this.id = id;
        return this;
    }

    public UpdateThingAction setMeta(NutMap meta) {
        this.meta = meta;
        return this;
    }

    public UpdateThingAction setName(String th_nm) {
        this.th_nm = th_nm;
        return this;
    }

    @Override
    public WnObj invoke() {
        // 得到对应对 Thing
        WnObj oT = this.checkThIndex(id);

        // 确保 Thing 是可用的
        if (oT.getInt("th_live") != Things.TH_LIVE) {
            throw Er.create("e.cmd.thing.updateDead", oT.id());
        }

        // 名称
        if (!Strings.isBlank(th_nm)) {
            meta.put("th_nm", th_nm);
        }

        // 更新这个 Thing
        io.appendMeta(oT, meta);

        // 记录输出
        this.output = oT;
        return output;
    }

}