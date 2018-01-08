package org.nutz.walnut.ext.wup.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 获取节点配置数据
 * @author wendal
 *
 */
public class wup_node_get implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        String macid = hc.params.check("macid").toUpperCase();
        String confsDir = Wn.normalizeFullPath("~/wup/confs/", sys);
        WnObj confs = sys.io.createIfNoExists(null, confsDir, WnRace.DIR);
        WnObj node = sys.io.fetch(confs, macid + ".json");
        if (node == null) {
            node = sys.io.query(Wn.Q.pid(confs.id()).setv("macid", macid)).get(0);
        }
        sys.out.print(sys.io.readText(node));
        if (hc.params.has("updated")) {
            String pkg = hc.params.get("updated");
            sys.exec("websocket text id:"+node.id()+" '" + Json.toJson(new NutMap("pkg", pkg), JsonFormat.compact()) + "'");
        }
    }

}
