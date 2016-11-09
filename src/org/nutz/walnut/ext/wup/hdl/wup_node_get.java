package org.nutz.walnut.ext.wup.hdl;

import org.nutz.walnut.api.io.WnObj;
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
        WnObj confObj = sys.io.check(null, Wn.normalizeFullPath("~/wup/confs/" + macid + ".json", sys));
        if (!key.equals(confObj.getString("vkey"))) {
            sys.err.print("key miss match!!");
            return;
        }
        sys.out.print(sys.io.readText(confObj));
    }

}
