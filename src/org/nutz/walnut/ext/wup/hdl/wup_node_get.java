package org.nutz.walnut.ext.wup.hdl;

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
        String key = hc.params.check("key");
        String confsDir = Wn.normalizeFullPath("~/wup/confs/", sys);
        WnObj confs = sys.io.createIfNoExists(null, confsDir, WnRace.DIR);
        WnObj confObj = sys.io.fetch(confs, macid + ".json");
        if (confObj == null) {
            confObj = sys.io.query(Wn.Q.pid(confs.id()).setv("macid", macid)).get(0);
        }
        if (!key.equals(confObj.getString("vkey"))) {
            //sys.err.print("key miss match!!");
            //return;
        }
        sys.out.print(sys.io.readText(confObj));
    }

}
