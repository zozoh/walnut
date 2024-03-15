package com.site0.walnut.ext.data.thing.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.api.WnExecutable;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.data.thing.ThingAction;
import com.site0.walnut.ext.data.thing.util.ThingConf;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class DeleteThingAction extends ThingAction<List<WnObj>> {

    protected Collection<String> ids;

    protected boolean hard;

    protected ThingConf conf;

    protected WnExecutable executor;

    /**
     * 查询条件（如果未指定 ids）则采用它
     */
    protected WnQuery query;

    /**
     * 指定一个安全删除数量，如果超过这个数量则拒绝执行
     */
    protected int maxSafeCount;

    /**
     * 采用 AutoMatch 进一步过滤
     */
    protected Object match;

    protected WnTmpl cmdTmpl;

    public DeleteThingAction setIds(Collection<String> ids) {
        this.ids = ids;
        return this;
    }

    public DeleteThingAction setIds(String... ids) {
        this.ids = Wlang.list(ids);
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

    public DeleteThingAction setExecutor(WnExecutable executor, WnTmpl cmdTmpl) {
        this.executor = executor;
        this.cmdTmpl = cmdTmpl;
        return this;
    }

    public DeleteThingAction setExecutor(WnExecutable executor) {
        this.executor = executor;
        return this;
    }

    public DeleteThingAction setQuery(WnQuery query) {
        this.query = query;
        return this;
    }

    public void setMaxSafeCount(int maxSafeCount) {
        this.maxSafeCount = maxSafeCount;
    }

    public DeleteThingAction setMatch(Object match) {
        this.match = match;
        return this;
    }

    @Override
    public List<WnObj> invoke() {
        // 准备返回结果
        List<WnObj> output = new LinkedList<>();

        //
        // 明确指定 ID 的删除
        //
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
                    WnObj oT = this.checkThIndex(thId);
                    __deleteOne(output, oData, oT);
                }
            }
        }
        //
        // 指定了一个查询条件的删除
        //
        else if (null != this.query && !this.query.isEmptyMatch()) {
            WnObj oIndex = this.checkDirTsIndex();
            this.query.setvToList("pid", oIndex.id());
            List<WnObj> oTList = io.query(this.query);
            if (!oTList.isEmpty()) {
                if (maxSafeCount == 0 || oTList.size() < maxSafeCount) {
                    // 数据目录的主目录
                    WnObj oData = this.checkDirTsData();

                    // 逐个删除
                    for (WnObj oT : oTList) {
                        __deleteOne(output, oData, oT);
                    }
                }
            }
        }

        // 返回输出
        return output;

    }

    private void __deleteOne(List<WnObj> output, WnObj oData, WnObj oT) {
        // 看看是否匹配给定条件
        if (null != this.match) {
            WnMatch wm = new AutoMatch(this.match);
            if (wm.match(oT)) {
                throw Er.create("e.cmd.thing.EvilDelete", oT.id());
            }
            // if (!match.match(oT)) {
            // throw Er.create("e.cmd.thing.EvilDelete", oT.id());
            // }
        }

        // 删除前的回调，控制删除
        Things.runCommands(oT, conf.getOnBeforeDelete(), executor);

        // 硬删除，或者已经是删除的了，那么真正的删除数据对象
        if (this.hard || oT.getInt("th_live", 0) == Things.TH_DEAD) {
            // 删除数据对象
            WnObj oThData = io.fetch(oData, oT.myId());
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
