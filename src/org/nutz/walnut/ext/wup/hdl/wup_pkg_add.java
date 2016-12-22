package org.nutz.walnut.ext.wup.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

/**
 * 添加一个更新包
 * @author wendal
 *
 */
public class wup_pkg_add implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        List<NutMap> list = new ArrayList<>();
        for (String path : hc.params.vals) {
            path = Wn.normalizeFullPath(path, sys);
            WnObj _src = sys.io.check(null, path);
            sys.io.walk(_src, (wobj) -> {
                String source = wobj.path();
                if (!source.endsWith(".tgz")) {
                    return;
                }
                String[] tmp = wobj.name().split("-", 2);
                if (tmp.length != 2 || tmp[0].isEmpty() || tmp[1].length() < 4)
                    return;
                String name = tmp[0];
                String version = tmp[1].substring(0, tmp[1].length() - 4);
                
                String dst = Wn.normalizePath("~/wup/pkgs/"+name+"/"+version+".tgz", sys);
                WnObj obj = sys.io.createIfNoExists(null, dst, WnRace.FILE);
                sys.io.writeAndClose(obj, sys.io.getInputStream(wobj, 0));
                NutMap re = new NutMap("name", name).setv("version", version);
                list.add(re);
            }, WalkMode.LEAF_ONLY);
        }
        sys.out.println(Json.toJson(list));
        if (hc.params.has("u")) {
            String[] updates = hc.params.get("u").split(",");
            for (NutMap re : list) {
                for (String update : updates) {
                    if (update.equals(re.get("name"))) {
                        
                    }
                }
            }
        }
    }

}
