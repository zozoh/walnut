package org.nutz.walnut.ext.data.thing.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.ThingAction;
import org.nutz.walnut.ext.data.thing.util.ThOtherUpdating;
import org.nutz.walnut.ext.data.thing.util.ThingConf;
import org.nutz.walnut.ext.data.thing.util.ThingUniqueKey;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class UpdateThingAction extends ThingAction<List<WnObj>> {

    protected WnExecutable executor;

    protected List<String> ids;

    protected NutMap meta;

    protected Object match;

    protected ThingConf conf;

    protected boolean withoutHook;

    public UpdateThingAction addIds(String... ids) {
        if (null == this.ids) {
            this.ids = new LinkedList<>();
        }
        for (String id : ids) {
            this.ids.add(id);
        }
        return this;
    }

    public UpdateThingAction setIds(List<String> ids) {
        this.ids = ids;
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

    public UpdateThingAction setExecutor(WnExecutable executor) {
        this.executor = executor;
        return this;
    }

    public UpdateThingAction setMatch(Object match) {
        this.match = match;
        return this;
    }

    public void setWithoutHook(boolean withoutHook) {
        this.withoutHook = withoutHook;
    }

    @Override
    public List<WnObj> invoke() {
        if (this.ids == null) {
            return null;
        }
        List<WnObj> objs = new ArrayList<>(this.ids.size());
        for (String id : this.ids) {
            WnObj o = this.updateOne(id);
            objs.add(o);
        }
        return objs;
    }

    private WnObj updateOne(String id) {
        // 复制一份元数据
        NutMap meta = this.meta.duplicate();

        // 得到对应对 Thing
        WnObj oT = this.checkThIndex(id);

        // 看看是否匹配给定条件
        if (null != this.match) {
            WnMatch wm = new AutoMatch(this.match);
            if (wm.match(oT)) {
                throw Er.create("e.cmd.thing.EvilUpdate", oT.id());
            }
            // if (!match.match(oT)) {
            // throw Er.create("e.cmd.thing.EvilUpdate", oT.id());
            // }
        }

        // 确保 Thing 是可用的
        if (oT.getInt("th_live") != Things.TH_LIVE) {
            throw Er.create("e.cmd.thing.updateDead", oT.id());
        }

        // 准备回调上下文
        NutMap context = new NutMap();
        context.put("old", oT.clone());
        context.put("update", meta);
        context.put("obj", oT);

        // 更新前的回调
        if (null != this.executor && !this.withoutHook) {
            Things.runCommands(context, conf.getOnBeforeUpdate(), executor);
        }

        // 根据唯一键约束检查重复
        if (conf.hasUniqueKeys()) {
            WnObj oIndex = this.checkDirTsIndex();
            ThingUniqueKey tuk = checkDuplicated(oIndex, meta, oT, conf.getUniqueKeys(), true);
            if (null != tuk) {
                throw Er.create("e.thing.ukey.duplicated", tuk.toString(oT.setAll(meta)));
            }
        }

        // 根据链接键，修改对应的键值
        List<ThOtherUpdating> others = evalOtherUpdating(oT, meta, this.conf, this.executor);

        // 更新这个 Thing
        NutMap metaForUpdate = new NutMap();
        metaForUpdate.putAll(meta);
        io.appendMeta(oT, metaForUpdate, conf.isUpdateKeepType());

        // 看看是否需要重新获取一下 Thing
        boolean reget = false;

        // 更新其他记录
        if (null != others && others.size() > 0) {
            for (ThOtherUpdating other : others) {
                other.doUpdate();
            }
            reget = true; // 标记要重新获取
        }

        // 看看是否有附加的创建执行脚本
        if (null != this.executor && !this.withoutHook) {
            Things.runCommands(context, conf.getOnUpdated(), executor);
        }

        if (!reget && !this.withoutHook) {
            if (null != conf.getOnBeforeUpdate() && conf.getOnBeforeUpdate().length > 0) {
                reget = true;
            }
            if (null != conf.getOnUpdated() && conf.getOnUpdated().length > 0) {
                reget = true;
            }
        }

        // 重新获取
        if (reget) {
            return this.checkThIndex(oT.id());
        }

        // 就这样返回即可
        return oT;
    }

}
