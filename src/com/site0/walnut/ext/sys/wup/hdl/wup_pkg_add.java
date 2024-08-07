package com.site0.walnut.ext.sys.wup.hdl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WalkMode;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

/**
 * 从一个文件夹添加更新包
 * 
 * @author wendal
 *
 */
public class wup_pkg_add implements JvmHdl {

    @SuppressWarnings("unchecked")
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        List<NutMap> list = new ArrayList<>();
        File dir = new File("/root/dw");
        for (File f : dir.listFiles()) {
            if (!f.getName().endsWith(".tgz")) {
                continue;
            }
            String[] tmp = f.getName().split("-", 2);
            if (tmp.length != 2 || tmp[0].isEmpty() || tmp[1].length() < 4)
                return;
            String name = tmp[0];
            String version = tmp[1].substring(0, tmp[1].length() - 4);

            // TODO 做成可配置
            String pkgDir = "/opt/oss/stones/wup/pkgs/" + name + "/";
            String path = pkgDir + version + ".tgz";
            File dst = new File(path);
            Files.createDirIfNoExists(pkgDir);
            Files.copy(f, dst);
            Files.write(path + ".sha1", Wlang.sha1(f));
            list.add(new NutMap("name", name).setv("version", version));
            if (new File(f.getPath() + ".zsync").exists()) {
                Files.copy(new File(f.getPath() + ".zsync"), new File(pkgDir + version + ".tgz.zsync"));
            }
        }
        if (hc.params.get("old") != null) {
            String path = hc.params.vals[0];
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

                String dst = Wn.normalizePath("~/wup/pkgs/" + name + "/" + version + ".tgz", sys);
                WnObj obj = sys.io.createIfNoExists(null, dst, WnRace.FILE);
                sys.io.writeAndClose(obj, sys.io.getInputStream(wobj, 0));
                sys.io.appendMeta(obj, "md:" + 493); // 0755
                list.add(new NutMap("name", name).setv("version", version));

                // 如果存在zsync文件,设置为可用呗
                WnObj zsync = sys.io.fetch(null, wobj.path() + ".zsync");
                if (zsync != null) {
                    dst = Wn.normalizePath("~/wup/pkgs/" + name + "/" + version + ".tgz.zsync", sys);
                    obj = sys.io.createIfNoExists(null, dst, WnRace.FILE);
                    sys.io.writeAndClose(obj, sys.io.getInputStream(zsync, 0));
                    sys.io.appendMeta(obj, "md:" + 493); // 0755
                }
            }, WalkMode.LEAF_ONLY);
        }
        sys.out.println(Json.toJson(list));
        if (hc.params.vals.length > 1) {
            for (int i = 1; i < hc.params.vals.length; i++) {
                WnObj wobj = sys.io.check(null, Wn.normalizeFullPath("~/wup/confs/"+hc.params.val(i) + ".json", sys));
                sys.out.println("updating " + wobj.path());
                NutMap map = sys.io.readJson(wobj, NutMap.class);
                List<NutMap> pkgs = (List<NutMap>) map.getOrDefault("pkgs", new ArrayList<NutMap>());
                for (NutMap pkg : pkgs) {
                    for (NutMap _pkg : list) {
                        if (pkg.getString("name").equals(_pkg.getString("name"))) {
                            pkg.put("version", _pkg.getString("version"));
                        }
                    }
                }
                map.put("pkgs", pkgs);
                sys.io.writeJson(wobj, map, JsonFormat.full());
            }
        }
    }

}
