package com.site0.walnut.ext.sys.wup.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

/**
 * 获取一个更新包
 * @author wendal
 *
 */
public class wup_pkg_info implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnObj pkg = fetchPkg(sys, hc);
        if (pkg == null)
            return;
        sys.out.writeJson(pkg);
    }

    protected WnObj fetchPkg(WnSystem sys, JvmHdlContext hc) {
        String macid = hc.params.check("macid").toUpperCase();
        String key = hc.params.check("key");
        String confsDir = Wn.normalizeFullPath("~/wup/confs/", sys);
        WnObj confs = sys.io.createIfNoExists(null, confsDir, WnRace.DIR);
        WnObj confObj = sys.io.fetch(confs, macid + ".json");
        if (confObj == null) {
            confObj = sys.io.query(Wn.Q.pid(confs.id()).setv("macid", macid)).get(0);
        }
        if (!key.equals(confObj.getString("vkey"))) {
            //sys.err.print("key miss match!!");
            //return null;
        }
        String name = hc.params.check("name");
        String version = hc.params.get("version", "lastest");
        String path = Wn.normalizeFullPath("~/wup/pkgs/"+name+"/"+version+".tgz", sys);
        WnObj wobj = sys.io.check(null, path);
        if (wobj.isMount()) {
            WnObj wobj2 = sys.io.fetch(null, path + ".sha1");
            if (wobj2 != null) {
                wobj.sha1(sys.io.readText(wobj2).trim());
            }
        }
        return wobj;
    }
}
