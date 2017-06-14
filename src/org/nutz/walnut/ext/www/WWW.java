package org.nutz.walnut.ext.www;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public abstract class WWW {

    public static final String AT_SEID = "DSEID";

    public static NutMap read_conf(WnIo io, String grp) {
        String phConf = Wn.appendPath(Wn.getUsrHome(grp), ".www/www.conf");
        WnObj oConf = io.fetch(null, phConf);

        NutMap conf;
        if (null == oConf) {
            conf = new NutMap();
        } else {
            conf = io.readJson(oConf, NutMap.class);
        }
        return conf;
    }

    /**
     * 判断一个域名是否为主域名。
     * <p>
     * <code>xx.xx</code> 形式的域名为主域名
     * 
     * @param host
     *            域名
     * @return 是否为主域名。
     */
    public static boolean isMainHost(String host) {
        if (host.matches("^[a-zA-Z0-9_-]+[.][a-z]+$")) {
            return true;
        }
        return false;
    }

    private WWW() {}

    public static Set<String> pickHosts(JvmHdlContext hc) {
        Set<String> hosts = new HashSet<>();
        for (String host : hc.params.vals) {
            // 如果是 www.xx.xx 那么默认认为是 xx.xx
            if (host.startsWith("www.")) {
                host = host.substring("www.".length());
            }

            // 记录
            hosts.add(host);
        }
        return hosts;
    }

    public static WnObj getWWWHome(WnSystem sys, JvmHdlContext hc, String myName) {
        String ph = hc.params.get("p", Wn.appendPath("/home", myName, "www"));
        String aph = Wn.normalizeFullPath(ph, sys);
        WnObj oWWWHome = sys.io.createIfNoExists(null, aph, WnRace.DIR);
        return oWWWHome;
    }

    public static void output_www_list(WnSystem sys, JvmHdlContext hc, List<WnObj> list) {
        if (hc.params.is("o")) {
            // 作为数组输出
            if (list.size() > 1 || hc.params.is("l")) {
                sys.out.println(Json.toJson(list, hc.jfmt));
            }
            // 作为单个对象输出
            else if (list.size() == 1) {
                sys.out.println(Json.toJson(list.get(0), hc.jfmt));
            }
        }
    }
}
