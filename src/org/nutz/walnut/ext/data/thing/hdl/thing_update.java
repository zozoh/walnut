package org.nutz.walnut.ext.data.thing.hdl;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.thing.WnThingService;
import org.nutz.walnut.ext.data.thing.util.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

@JvmHdlParamArgs(value = "cnqlVNHQ", regex = "^(quiet|overwrite)$")
public class thing_update implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 找到集合
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);

        // 得到字段
        String json = Cmds.getParamOrPipe(sys, hc.params, "fields", false);
        NutMap meta = Strings.isBlank(json) ? new NutMap() : Lang.map(json);
        Things.formatMeta(meta);

        // 分析参数
        String[] ids = hc.params.vals;
        NutMap match = hc.params.getMap("match");

        // 看看是否需要safe机制
        if (hc.params.has("safe")) {
            NutMap safe = null;
            // 如果是true,使用thing set的设置项
            if ("true".equals(hc.params.get("safe"))) {
                safe = oTs.getAs("th_safe", NutMap.class);
            }
            // 否则肯定是个map
            else {
                safe = Lang.map(hc.params.get("safe"));
            }
            if (safe != null) {
                String actived = safe.getString("actived");
                Pattern act = Strings.isBlank(actived) ? null : Regex.getPattern(actived);
                String locked = safe.getString("locked");
                Pattern lock = Strings.isBlank(locked) ? null : Regex.getPattern(locked);
                for (String key : new HashSet<>(meta.keySet())) {
                    if (act != null && !act.matcher(key).find())
                        meta.remove(key);
                    if (lock != null && lock.matcher(key).find())
                        meta.remove(key);
                }
            }
        }

        // 准备调用接口
        List<WnObj> list = wts.updateThings(ids, meta, sys, match);

        // 如果是一个对象，并且并未强制 -l，则输出第一个对象
        if (list.size() == 1 && !hc.params.is("l")) {
            hc.output = list.get(0);
        }
        // 那么就保持输出为一个列表
        else {
            hc.output = list;
        }
    }

}
