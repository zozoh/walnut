package org.nutz.walnut.ext.thing.impl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.ThingAction;
import org.nutz.walnut.ext.thing.util.ThingConf;
import org.nutz.walnut.ext.thing.util.ThingUniqueKey;
import org.nutz.walnut.ext.thing.util.Things;

public class UpdateThingAction extends ThingAction<WnObj> {

    private String id;

    private NutMap meta;

    private ThingConf conf;

    public UpdateThingAction setId(String id) {
        this.id = id;
        return this;
    }

    public UpdateThingAction setMeta(NutMap meta) {
        this.meta = meta;
        return this;
    }

    public UpdateThingAction setConf(ThingConf conf) {
        this.conf = conf;
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

        // 根据唯一键约束检查重复
        if (conf.hasUniqueKeys()) {
            WnObj oIndex = this.checkDirTsIndex();
            ThingUniqueKey tuk = checkDuplicated(oIndex, meta, oT, conf.getUniqueKeys(), true);
            if (null != tuk) {
                throw Er.create("e.thing.ukey.duplicated", tuk.toString());
            }
        }

        // 更新这个 Thing
        io.appendMeta(oT, meta);

        // 返回
        return oT;
    }

}
