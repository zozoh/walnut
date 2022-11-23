package org.nutz.walnut.ext.data.thing.hdl;

import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.ThingDuplicateOptions;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.validate.impl.AutoMatch;

@JvmHdlParamArgs(value = "cqn", regex = "(shallow|obj|nofiles)")
public class thing_duplicate implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 分析参数
        String thId = hc.params.val_check(0);
        ThingDuplicateOptions opt = new ThingDuplicateOptions();
        opt.dupCount = hc.params.val_int(1, 1);
        opt.toKey = hc.params.getString("tokey", "id");
        opt.shallow = hc.params.is("shallow");
        opt.fixedMeta = hc.params.getMap("meta");

        // 指定记录要复制的字段
        String fields = hc.params.getString("fields", null);
        opt.fieldFilter = AutoMatch.parse(fields, true);

        // 复制文件时要copy的字段
        String fflds = hc.params.getString("fflds");
        if (!Ws.isBlank(fflds)) {
            opt.fFieldMatch = AutoMatch.parse(fflds, false);
        }

        String fmeta = hc.params.getString("fmeta");
        if (!Ws.isBlank(fmeta)) {
            opt.fmeta = Lang.map(fmeta);
        }

        // 指定复制目标
        if (hc.params.has("to")) {
            String toIds = hc.params.getString("to");
            opt.toIds = Ws.splitIgnoreBlanks(toIds);
        }

        // 固定复制的文件
        String copy = hc.params.getString("copy");
        if (!Ws.isBlank(copy)) {
            opt.copyFiles = Ws.splitIgnoreBlank(copy);
        }

        //
        // 集合外引用文件的复制设置
        //
        String foutside = hc.params.getString("foutside");
        if (!Ws.isBlank(foutside)) {
            opt.fOutside = Pattern.compile(foutside);

            opt.fMatchOnly = hc.params.is("fmatch-only");

            String fvars = hc.params.getString("fvars");
            if (!Ws.isBlank(fvars)) {
                opt.fvars = Lang.map(fvars);
            }

            String fnewname = hc.params.getString("fnewname", "${id}-${name?@input}");
            opt.fNewname = Tmpl.parse(fnewname);
        }

        // 准备服务类
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);

        // 调用接口
        List<WnObj> list = wts.duplicateThing(sys, thId, opt);

        // 准备输出
        if (hc.params.is("obj") && list.size() == 1) {
            hc.output = list.get(0);
        }
        // 只输出一个
        else {
            hc.output = list;
        }
    }

}
