package org.nutz.walnut.ext.seq.hdl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

@JvmHdlParamArgs(value="cqn", regex="^(force)$")
public class seq_create implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 分析输入变量
        NutMap vars = new NutMap();

        // 用户定义了变量
        if (hc.params.has("vars")) {
            String vstr = hc.params.get("vars");
            // 从管道读取
            if ("~pipe".equals(vstr) || "true".equals(vstr)) {
                vstr = sys.in.readAll();
            }
            vars = Lang.map(vstr);
        }
        // 预防null
        if (vars == null) {
            vars = new NutMap();
        }
        
        String tmpl = hc.params.check("tmpl");
        int start = hc.params.getInt("start", 1); // 起始
        int count = hc.params.getInt("count", 100); // 默认生成100个,差不多了吧
        int step = hc.params.getInt("step", 1); // 步进长度
        String list_match = hc.params.get("match"); // 列表匹配模式
        
        
        if (step == 0)
            step = 1;
        
        int current = start;
        //Tmpl t = Tmpl.parse(tmpl);
        Tmpl t = Cmds.parse_tmpl(tmpl);
        // 简单序列模式
        if (Strings.isBlank(list_match)) {
            for (int i = 0; i < count; i++) {
                vars.put("seq", current);
                sys.out.print(t.render(vars) + ",");
                current += step;
            }
        }
        else {
            boolean force = hc.params.is("force");    // 是否覆盖原有的值
            String sort = hc.params.get("sort", "ct:1"); // 列表排序模式,
            String list_key = hc.params.get("key", "u_code"); // 列表模式下的存储序列值的键
            List<WnObj> objs = null;
            int updated = 0;
            int nochange = 0;
            if (Strings.isNotBlank(list_match)) {
                WnQuery q = new WnQuery();
                q.add(Lang.map(list_match));
                q.setv("d0", "home").setv("d1", sys.me.mainGroup());
                q.sort(Lang.map(sort));
                objs = sys.io.query(q);
                if (objs.isEmpty())
                    return;
            }
            Set<String> exitKeys = new HashSet<>();
            if (!force) {
                for (WnObj wobj : objs) {
                    if (wobj.get(list_key) != null)
                        exitKeys.add(wobj.getString(list_key));
                }
            }
            Iterator<WnObj> it = objs.iterator();
            WnObj cur = it.next();
            while (true) {
                if (!force && cur.get(list_key) != null) {
                    nochange++;
                    if (it.hasNext()) {
                        cur = it.next();
                        continue;
                    }
                    else {
                        break;
                    }
                }
                vars.put("seq", current);
                vars.put("obj", cur);
                String value = t.render(vars);
                if (exitKeys.contains(value)) {
                    current += step;
                    continue;
                }
                sys.io.appendMeta(cur, new NutMap(list_key, value));
                updated ++;
                current += step;
                if (it.hasNext())
                    cur = it.next();
                else
                    break;
            }
            sys.out.writeJson(new NutMap("updated", updated).setv("nochange", nochange), Cmds.gen_json_format(hc.params));
        }
    }
}
