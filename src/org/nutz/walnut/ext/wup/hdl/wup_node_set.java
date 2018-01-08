package org.nutz.walnut.ext.wup.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 设置节点配置
 * @author wendal
 *
 */
public class wup_node_set implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        String macid = hc.params.check("macid").toUpperCase();
        String pkg = hc.params.check("pkg");
        String version = hc.params.check("version");
        String confsDir = Wn.normalizeFullPath("~/wup/confs/", sys);
        WnObj confs = sys.io.createIfNoExists(null, confsDir, WnRace.DIR);
        WnObj wobj = sys.io.check(confs, macid);
        NutMap node = sys.io.readJson(wobj, NutMap.class);
        List<NutMap> pkgs = node.getAsList("pkgs", NutMap.class);
        if (pkgs == null) {
            pkgs = new ArrayList<>();
            node.put("pkgs", pkgs);
        }
        boolean exist = false;
        for (NutMap _pkg : pkgs) {
            if (pkg.equals(_pkg.get("name"))) {
                _pkg.put("version", version);
                exist = true;
            }
        }
        if (!exist) {
            pkgs.add(new NutMap("name", pkg).setv("version", version));
        }
        sys.io.writeJson(wobj, node, JsonFormat.full());
    }

}
