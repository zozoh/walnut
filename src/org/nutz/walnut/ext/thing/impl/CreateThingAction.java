package org.nutz.walnut.ext.thing.impl;

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
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.ThingAction;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.util.Wn;

public class CreateThingAction extends ThingAction<List<WnObj>> {

    private String uniqueKey;

    private List<NutMap> metas;

    private Tmpl process;

    private WnOutputable out;

    private WnExecutable executor;

    private Tmpl cmdTmpl;

    private NutMap fixedMeta;

    public CreateThingAction() {
        this.metas = new LinkedList<>();
    }

    public CreateThingAction setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
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

    public CreateThingAction setFixedMeta(NutMap fixedMeta) {
        this.fixedMeta = fixedMeta;
        return this;
    }

    @Override
    public List<WnObj> invoke() {
        // 找到索引
        WnObj oIndex = this.checkDirTsIndex();
        int len = metas.size();

        // 进度模式
        if (null != process && null != out) {
            int i = 1;
            for (NutMap meta : metas) {
                String P = String.format("%%[%d/%d]", i++, len);
                WnObj oT = __create_one(oIndex, meta);
                String msg = process.render(oT.setv("P", P).setv("I", i));
                out.println(msg);
            }
            out.println(Strings.dup('-', 20));
            out.printlnf("All done for %d records", i);
            return null;
        }
        // 普通创建模式
        else {
            // 准备返回值
            List<WnObj> list = new ArrayList<>(len);

            // 来吧，循环生成
            for (NutMap meta : metas) {
                WnObj oT = __create_one(oIndex, meta);
                list.add(oT);
            }

            // 返回
            return list;
        }
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

        // 默认增加固定字段
        if (null != this.fixedMeta && this.fixedMeta.size() > 0) {
            meta.putAll(this.fixedMeta);
        }

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

        // 执行完毕的回调
        if (null != this.executor && null != this.cmdTmpl) {
            String cmdText = cmdTmpl.render(oT);
            String json = Json.toJson(oT, JsonFormat.full().setQuoteName(true));
            StringBuilder stdOut = new StringBuilder();
            StringBuilder stdErr = new StringBuilder();
            this.executor.exec(cmdText, stdOut, stdErr, json);

            // 出错就阻止后续执行
            if (stdErr.length() > 0)
                throw Er.create("e.cmd.thing.after_create", stdErr);

            // 正常输出
            if (null != out) {
                out.printf("  - after OK: %s", stdOut);
            }
        }

        // 返回
        return oT;
    }

}
