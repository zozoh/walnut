package com.site0.walnut.ext.data.thing.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.WnExecutable;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.ext.data.thing.options.ThCreateOptions;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Ws;

@JvmHdlParamArgs(value = "cnqihbslVNHQ", regex = "^(nohook)$")
public class thing_create implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 找到集合
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);

        // 得到字段
        String ukey = hc.params.getString("unique", null);
        String json = Cmds.getParamOrPipe(sys, hc.params, "fields", false);
        String process = hc.params.getString("process", null);
        if ("<auto>".equals(process)) {
            process = "${P}: ${id} : ${title|abbr|nickname|nm}";
        }

        // 准备后续执行
        String afterCmd = hc.params.getString("after", null);
        WnExecutable exec = sys;

        // 得到固定数据
        NutMap fixedMeta = hc.params.getMap("fixed");

        // 数组：那么就表示创建多条数据咯
        if (Ws.isQuoteBy(json, '[', ']')) {
            List<NutMap> list = Json.fromJsonAsList(NutMap.class, json);
            // 格式化一下传入的字符串宏
            if (!list.isEmpty())
                for (NutMap meta : list) {
                    Things.formatMeta(meta);
                }
            ThCreateOptions opt = ThCreateOptions.create(ukey,
                                                         fixedMeta,
                                                         sys.out,
                                                         process,
                                                         exec,
                                                         afterCmd);
            opt.withoutHook = hc.params.is("nohook");
            hc.output = wts.createManyThings(list, opt);
        }
        // 普通对象: 表示创建一条数据
        else if (!Ws.isBlank(json)) {
            // 错误字符串，打印到错误输出流
            if (json.startsWith("e.")) {
                sys.err.print(json);
                return;
            }

            NutMap meta = Wlang.map(json);
            Things.formatMeta(meta);
            // 执行创建
            ThCreateOptions opt = ThCreateOptions.create(ukey, fixedMeta, exec, afterCmd);
            opt.withoutHook = hc.params.is("nohook");
            hc.output = wts.createOneThing(meta, opt);
        }

    }

}
