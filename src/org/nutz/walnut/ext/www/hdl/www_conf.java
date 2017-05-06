package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class www_conf implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 读取配置文件
        WnObj oConf = Wn.getObj(sys, "~/.www/www.conf");
        NutMap conf;

        if (oConf != null) {
            conf = sys.io.readJson(oConf, NutMap.class);
        }
        // 没有配置文件 ...
        else {
            conf = new NutMap();
        }

        // 设定默认值
        conf.putDefault("dns_r_A", "127.0.0.1");
        conf.putDefault("login_ok", "/");
        conf.putDefault("login_fail", "/login_fail.wnml");
        conf.putDefault("logout", "/");

        // 输出
        sys.out.println(Json.toJson(conf, hc.jfmt));
    }

}
