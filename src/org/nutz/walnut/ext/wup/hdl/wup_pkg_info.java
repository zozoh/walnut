package org.nutz.walnut.ext.wup.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

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
        return sys.io.check(null, path);
    }
}
