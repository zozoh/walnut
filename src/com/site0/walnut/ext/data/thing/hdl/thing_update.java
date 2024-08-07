package com.site0.walnut.ext.data.thing.hdl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.thing.WnThingService;
import com.site0.walnut.ext.data.thing.options.ThUpdateOptions;
import com.site0.walnut.ext.data.thing.util.Things;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wlang;

@JvmHdlParamArgs(value = "cnqlVNHQ", regex = "^(quiet|nohook)$")
public class thing_update implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 找到集合
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnThingService wts = new WnThingService(sys, oTs);

        // 得到字段
        String json = Cmds.getParamOrPipe(sys, hc.params, "fields", false);
        // 空字符串防守一把
        if (Ws.isBlank(json)) {
            return;
        }
        // 错误字符串，打印到错误输出流
        if (json.startsWith("e.")) {
            sys.err.print(json);
            return;
        }
        // 得到元数据
        NutMap meta = Wlang.map(json);
        Things.formatMeta(meta);

        // 分析参数
        String[] ids = hc.params.vals;
        NutMap match = hc.params.getMap("match");

        // 分析ID 多个ID 可用半角逗号分隔
        List<String> idList = new LinkedList<>();
        for (String id : ids) {
            String[] ss = Ws.splitIgnoreBlank(id);
            for (String s : ss) {
                idList.add(s);
            }
        }
        ids = new String[idList.size()];
        idList.toArray(ids);

        // 看看是否需要safe机制
        if (hc.params.has("safe")) {
            NutMap safe = null;
            // 如果是true,使用thing set的设置项
            if ("true".equals(hc.params.get("safe"))) {
                safe = oTs.getAs("th_safe", NutMap.class);
            }
            // 否则肯定是个map
            else {
                safe = Wlang.map(hc.params.get("safe"));
            }
            if (safe != null) {
                String actived = safe.getString("actived");
                Pattern act = Ws.isBlank(actived) ? null : Regex.getPattern(actived);
                String locked = safe.getString("locked");
                Pattern lock = Ws.isBlank(locked) ? null : Regex.getPattern(locked);
                for (String key : new HashSet<>(meta.keySet())) {
                    if (act != null && !act.matcher(key).find())
                        meta.remove(key);
                    if (lock != null && lock.matcher(key).find())
                        meta.remove(key);
                }
            }
        }

        // 准备调用接口
        ThUpdateOptions opt = ThUpdateOptions.create(meta, sys, match);
        opt.withoutHook = hc.params.is("nohook");
        List<WnObj> list = wts.updateManyThings(ids, opt);

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
