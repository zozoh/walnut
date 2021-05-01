package org.nutz.walnut.ext.sys.wup.hdl;

import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 初始化一个节点
 * @author wendal
 *
 */
public class wup_node_init implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        String macid = hc.params.check("macid").toUpperCase();
        String type = hc.params.get("type");
        String default_config = Strings.isBlank(type) ? "default.json" : "default_" + type + ".json";
        //String godkey = hc.params.check("godkey");
        WnObj confDir = sys.io.check(null, Wn.normalizeFullPath("~/wup/confs/", sys));
        WnObj confObj = sys.io.createIfNoExists(confDir, macid + ".json", WnRace.FILE);
        if (!confObj.containsKey("vkey")) {
            confObj.setv("vkey", R.UU32());
            sys.io.appendMeta(confObj, new NutMap("vkey", confObj.get("vkey")).setv("macid", macid));
        }
        sys.io.writeText(confObj, sys.io.readText(sys.io.check(confDir, default_config)));
        sys.out.writeJson(new NutMap("key", confObj.get("vkey")), JsonFormat.full());
    }

}
