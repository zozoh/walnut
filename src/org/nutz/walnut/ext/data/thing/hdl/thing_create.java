package org.nutz.walnut.ext.data.thing.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.WnExecutable;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

@JvmHdlParamArgs("cnqihbslVNHQ")
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

        // 准备后续执行
        String afterCmd = hc.params.getString("after", null);
        WnExecutable exec = sys;

        // 得到固定数据
        NutMap fixedMeta = hc.params.getMap("fixed");

        // 数组：那么就表示创建多条数据咯
        if (Strings.isQuoteBy(json, '[', ']')) {
            List<NutMap> list = Json.fromJsonAsList(NutMap.class, json);
            // 格式化一下传入的字符串宏
            if (!list.isEmpty())
                for (NutMap meta : list) {
                    Things.formatMeta(meta);
                }
            hc.output = wts.createThings(list, ukey, fixedMeta, sys.out, process, exec, afterCmd);
        }
        // 普通对象: 表示创建一条数据
        else {
            NutMap meta = Strings.isBlank(json) ? new NutMap() : Lang.map(json);
            Things.formatMeta(meta);
            // 执行创建
            hc.output = wts.createThing(meta, ukey, fixedMeta, exec, afterCmd);
        }

    }

}
