package org.nutz.walnut.ext.data.thing.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.ThingAction;
import org.nutz.walnut.ext.data.thing.util.ThingConf;
import org.nutz.walnut.ext.data.thing.util.Things;

public class DeleteThingAction extends ThingAction<List<WnObj>> {

    protected Collection<String> ids;

    protected boolean hard;

    protected ThingConf conf;

    protected WnExecutable executor;

    protected NutMap match;

    protected Tmpl cmdTmpl;

    public DeleteThingAction setIds(Collection<String> ids) {
        this.ids = ids;
        return this;
    }

    public DeleteThingAction setIds(String... ids) {
        this.ids = Lang.list(ids);
        return this;
    }

    public DeleteThingAction setHard(boolean hard) {
        this.hard = hard;
        return this;
    }

    public DeleteThingAction setConf(ThingConf conf) {
        this.conf = conf;
        return this;
    }

    public DeleteThingAction setExecutor(WnExecutable executor, Tmpl cmdTmpl) {
        this.executor = executor;
        this.cmdTmpl = cmdTmpl;
        return this;
    }

    public DeleteThingAction setExecutor(WnExecutable executor) {
        this.executor = executor;
        return this;
    }

    public DeleteThingAction setMatch(NutMap match) {
        this.match = match;
        return this;
    }

    @Override
    public List<WnObj> invoke() {
        // 准备返回结果
        List<WnObj> output = new LinkedList<>();

        if (null != ids && !ids.isEmpty()) {
            // 数据目录的主目录
            WnObj oData = this.checkDirTsData();

            for (String id : ids) {
                // 支持一下半角逗号分隔的 ID列表
                String[] thIds = Strings.splitIgnoreBlank(id);
                if (null == thIds || thIds.length == 0) {
                    continue;
                }
                // 逐个删除
                for (String thId : thIds) {
                    // 得到对应对 Thing
                    WnObj oT = this.checkThIndex(thId);

                    // 看看是否匹配给定条件
                    if (null != this.match) {
                        if (!match.match(oT)) {
                            throw Er.create("e.cmd.thing.EvilDelete", oT.id());
                        }
                    }

                    // 删除前的回调，控制删除
                    Things.runCommands(oT, conf.getOnBeforeDelete(), executor);

                    // 硬删除，或者已经是删除的了，那么真正的删除数据对象
                    if (this.hard || oT.getInt("th_live", 0) == Things.TH_DEAD) {
                        // 删除数据对象
                        WnObj oThData = io.fetch(oData, oT.id());
                        if (null != oThData) {
                            io.delete(oThData, true);
                        }
                        // 删除索引
                        io.delete(oT);
                        output.add(oT);
                    }
                    // 仅仅标记为删除
                    else {
                        oT.put("th_live", Things.TH_DEAD);
                        oT.put("th_set", oTs.id());
                        io.set(oT, "^th_live$");
                        output.add(oT);
                    }

                    // 删除后回调
                    Things.runCommands(oT, conf.getOnDeleted(), executor);
                }
            }
        }

        // 返回输出
        return output;

    }

}