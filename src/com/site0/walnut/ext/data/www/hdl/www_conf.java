package com.site0.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnSysConf;

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

        // 设置 DNS 转接地址
        if (!conf.has("dns_record")) {
            // 读取系统配置文件
            WnSysConf sysConf = Wn.getSysConf(sys.io);
            String dftHost = sysConf.getMainHost();
            conf.put("dns_record", dftHost);
        }

        // 设定其他默认值
        conf.putDefault("login_ok", "/");
        conf.putDefault("login_fail", "/login_fail.wnml");
        conf.putDefault("logout", "/");

        // 输出
        sys.out.println(Json.toJson(conf, hc.jfmt));
    }

}
