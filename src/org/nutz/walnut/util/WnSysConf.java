package org.nutz.walnut.util;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

public class WnSysConf {

    /**
     * 系统的访问主协议，可能是 http or https
     */
    private WnSysScheme mainScheme;

    /**
     * 系统访问的主地址，本地开发时，一般为 localhost
     */
    private String mainHost;

    /**
     * 系统访问的主端口，譬如 8080，当然生产环境下，通常为 80
     */
    private int mainPort;

    public NutMap toMap() {
        return (NutMap) Lang.obj2map(this);
    }

    /**
     * @return 系统访问的主机地址，譬如 `localhost:8080`
     */
    public String getMainHostAndPort() {
        String re = this.getMainHost();
        int port = this.getMainPort();
        if (port != 80) {
            re += ":";
            re += port;
        }
        return re;
    }

    /**
     * @return 系统访问的整体前缀，譬如 `http://localhost:8080`
     */
    public String getMainUrlBase() {
        String re = this.getMainScheme().toString();
        re += "://";
        re += this.getMainHostAndPort();
        return re;
    }

    public WnSysScheme getMainScheme() {
        return null == mainScheme ? WnSysScheme.http : mainScheme;
    }

    public void setMainScheme(WnSysScheme mainScheme) {
        this.mainScheme = mainScheme;
    }

    public String getMainHost() {
        return getMainHost("localhost");
    }

    public String getMainHost(String dftHost) {
        return Strings.sBlank(mainHost, dftHost);
    }

    public void setMainHost(String mainHost) {
        this.mainHost = mainHost;
    }

    public int getMainPort() {
        return this.getMainPort(8080);
    }

    public int getMainPort(int dftPort) {
        return mainPort <= 0 ? dftPort : mainPort;
    }

    public void setMainPort(int mainPort) {
        this.mainPort = mainPort;
    }

}
