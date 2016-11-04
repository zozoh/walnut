package org.nutz.walnut.ext.wup.hdl;

import org.nutz.walnut.api.io.WnObj;
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
        String macid = hc.params.check("macid");
        String key = hc.params.check("key");
        WnObj confObj = sys.io.check(null, Wn.normalizeFullPath("~/wup/confs/" + macid + ".json", sys));
        if (!key.equals(confObj.getString("vkey"))) {
            sys.err.print("key miss match!!");
            return null;
        }
        String name = hc.params.check("name");
        String version = hc.params.get("version", "lastest");
        String path = Wn.normalizeFullPath("~/wup/pkgs/"+name+"/"+version+".tgz", sys);
        return sys.io.check(null, path);
    }
}
