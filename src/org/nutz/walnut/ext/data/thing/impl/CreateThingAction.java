package org.nutz.walnut.ext.data.thing.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.ext.data.thing.ThingAction;
import org.nutz.walnut.ext.data.thing.util.ThOtherUpdating;
import org.nutz.walnut.ext.data.thing.util.ThingConf;
import org.nutz.walnut.ext.data.thing.util.ThingUniqueKey;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.web.WebException;

/**
 * @author zozoh(zozohtnt@gmail.com)
 */
public class CreateThingAction extends ThingAction<List<WnObj>> {

    protected String[] uniqueKeys;

    protected List<NutMap> metas;

    protected Tmpl process;

    protected WnOutputable out;

    protected WnExecutable executor;

    protected Tmpl cmdTmpl;

    protected NutMap fixedMeta;

    protected ThingConf conf;

    protected int dupCount = 0;

    public CreateThingAction() {
        this.metas = new LinkedList<>();
    }

    public CreateThingAction setUniqueKey(String uniqueKey) {
        this.uniqueKeys = Strings.splitIgnoreBlank(uniqueKey);
        return this;
    }

    public CreateThingAction setUniqueKeys(String... uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
        return this;
    }

    public CreateThingAction setProcess(WnOutputable out, String process) {
        this.out = out;
        this.process = Tmpl.parse(process);
        return this;
    }

    public CreateThingAction setProcess(WnOutputable out, Tmpl process) {
        this.out = out;
        this.process = process;
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

    public CreateThingAction setExecutor(WnExecutable executor, String cmdTmpl) {
        this.executor = executor;
        this.cmdTmpl = Tmpl.parse(cmdTmpl);
        return this;
    }

    public CreateThingAction setExecutor(WnExecutable executor, Tmpl cmdTmpl) {
        this.executor = executor;
        this.cmdTmpl = cmdTmpl;
        return this;
    }

    public CreateThingAction setExecutor(WnExecutable executor) {
        this.executor = executor;
        return this;
    }

    public CreateThingAction setFixedMeta(NutMap fixedMeta) {
        this.fixedMeta = fixedMeta;
        return this;
    }

    public CreateThingAction setConf(ThingConf conf) {
        this.conf = conf;
        return this;
    }

    @Override
    public List<WnObj> invoke() {
        // 找到索引
        WnObj oIndex = this.checkDirTsIndex();
        int len = metas.size();
        int i = 1;

        // 进度模式
        if (null != process && null != out) {
            for (NutMap meta : metas) {
                __create_one(oIndex, meta, i++, len);
            }
            out.println(Strings.dup('-', 20));
            out.printlnf("All done for %d records, %d is duplicated", i - 1, this.dupCount);
            return null;
        }
        // 普通创建模式
        else {
            // 准备返回值
            List<WnObj> list = new ArrayList<>(len);

            // 来吧，循环生成
            for (NutMap meta : metas) {
                WnObj oT = __create_one(oIndex, meta, i++, len);
                if (null != oT)
                    list.add(oT);
            }

            // 返回
            return list;
        }
    }

    private WnObj __create_one(WnObj oIndex, NutMap meta, int i, int len) {
        String P = String.format("%%[%d/%d]", i++, len);
        // 创建或者取得一个一个 Thing
        WnObj oT = null;
        boolean isDuplicated = false;

        // 默认增加固定字段
        if (null != this.fixedMeta && this.fixedMeta.size() > 0) {
            meta.putAll(this.fixedMeta);
        }

        // 检查一下字段等值
        try {
            // 看看如果声明了唯一键(导入)
            if (null != this.uniqueKeys && this.uniqueKeys.length > 0) {
                oT = this.checkUniqueKeys(oIndex, null, meta, this.uniqueKeys, true, false);
                // 如果查出了数据，那么证明是之前数据库里就有
                if (null != oT) {
                    isDuplicated = true;
                    this.dupCount++;
                }
            }

            // 根据唯一键约束检查重复
            if (conf.hasUniqueKeys()) {
                ThingUniqueKey tuk = checkDuplicated(oIndex, meta, oT, conf.getUniqueKeys(), false);
                if (null != tuk) {
                    throw Er.create("e.thing.ukey.duplicated", tuk.toString(meta));
                }
            }
        }
        catch (WebException e) {
            if (null != process && null != out) {
                out.printlnf("  !!! %s for %s", e.toString(), Json.toJson(meta));
            }
            // 如果就是创建单个数据的，那么应该不是导入，抛错吧
            if (1 == len)
                throw e;
            return null;
        }

        // zozoh: 不知道下面几行代码动机是啥，没有就不设置呗。靠，先注释掉
        // // 图标
        // if (!meta.has("icon") && !oT.has("icon"))
        // meta.put("icon", oTs.get("th_icon"));
        //
        // // 缩略图
        // if (!meta.has("thumb") && !oT.has("thumb") && oTs.has("th_thumb"))
        // meta.put("thumb", oTs.get("th_thumb"));

        // 设置更多的固有属性
        meta.put("th_set", oTs.id());
        meta.put("th_live", Things.TH_LIVE);

        // 默认的内容类型
        if (!meta.has("mime") && meta.has("tp"))
            meta.put("mime", io.mimes().getMime(meta.getString("tp"), "text/plain"));

        // 还木有，那么就创建咯
        if (null == oT) {
            oT = new WnIoObj();
            oT.putAll(meta);
            oT.putDefault("nm", "${id}");
            oT.race(WnRace.FILE);
        }

        // 根据链接键，自动修改元数据，返回的值是一组后续更新脚本
        List<ThOtherUpdating> others = evalOtherUpdating(oT, meta, this.conf, this.executor);

        NutMap context = null;

        // 更新这个 Thing （因为从 uniqueKeys 里得到了之前的记录，这样可以导入防重）
        if (oT.hasID()) {
            if (null != this.executor) {
                // 准备回调上下文（更新）
                context = new NutMap();
                context.put("old", oT.clone());
                context.put("update", meta);
                context.put("obj", oT);

                // 更新前的回调
                Things.runCommands(context, conf.getOnBeforeUpdate(), executor);
            }

            // 更新这个 Thing
            io.appendMeta(oT, meta);
        }
        // 创建一个
        else {
            oT = io.create(oIndex, oT);
        }

        // 看看是否需要重新获取 Thing
        boolean re_get = false;

        // 更新其他记录
        if (null != others && others.size() > 0) {
            re_get = true;
            for (ThOtherUpdating other : others) {
                other.doUpdate();
            }
        }

        // 如果是第一次创建，则执行附加脚本
        if (!isDuplicated) {
            // 看看是否有附加的创建执行脚本
            String[] on_created = conf.getOnCreated();
            if (null != this.executor && null != on_created && on_created.length > 0) {
                Things.runCommands(oT, on_created, executor);
                re_get = true;
            }
        }
        // 否则执行更新回调的附加脚本
        else if (null != context) {
            String[] on_updated = conf.getOnUpdated();
            if (null != on_updated && on_updated.length > 0) {
                Things.runCommands(context, on_updated, executor);
                re_get = true;
            }
        }

        if (null != process && null != out) {
            String msg = process.render(oT.setv("P", P).setv("I", i));
            out.println(msg);
        }

        // 执行完毕的回调也必须是不能重复执行的
        if (!isDuplicated && null != this.executor && null != this.cmdTmpl) {
            String cmdText = Strings.trim(cmdTmpl.render(oT));
            String input = null;
            if (cmdText.startsWith("|")) {
                cmdText = cmdText.substring(1);
                input = Json.toJson(oT, JsonFormat.compact().setQuoteName(true));
            }
            StringBuilder stdOut = new StringBuilder();
            StringBuilder stdErr = new StringBuilder();
            this.executor.exec(cmdText, stdOut, stdErr, input);

            // 出错就阻止后续执行
            if (stdErr.length() > 0)
                throw Er.create("e.cmd.thing.after_create", stdErr);

            // 正常输出
            if (null != out) {
                out.printlnf("  - after OK: %s", stdOut);
            }

            re_get = true;
        }

        // 输出一下重复
        if (isDuplicated) {
            if (null != out) {
                out.println("  - !! isDuplicated");
            }
        }

        if (re_get) {
            oT = this.io.get(oT.id());
            oT.put("th_set", oTs.id());
        }

        // 返回
        return oT;
    }

}
