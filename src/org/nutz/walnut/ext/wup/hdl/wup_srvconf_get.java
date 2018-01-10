package org.nutz.walnut.ext.wup.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 获取节点配置数据, 一般就是web_local.properties配置文件
 * @author wendal
 *
 */
public class wup_srvconf_get implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        String macid = hc.params.check("macid").toUpperCase();
        String confsDir = Wn.normalizeFullPath("~/wup/srvconfs/", sys);
        WnObj confs = sys.io.createIfNoExists(null, confsDir, WnRace.DIR);
        WnObj node = sys.io.query(Wn.Q.pid(confs.id()).setv("macid", macid)).get(0);
        sys.out.print(sys.io.readText(node));
    }

}
