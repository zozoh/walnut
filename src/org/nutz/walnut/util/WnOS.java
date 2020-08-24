package org.nutz.walnut.util;

import org.nutz.lang.hardware.Networks;
import org.nutz.walnut.api.err.Er;

public abstract class WnOS {

    public static void fillRuntimeInfo(WnSysRuntime rt) {
        try {
            // 本机信息
            rt.machineName = Networks.hostName();
            rt.localAddress = Networks.ipv4();
            rt.localMac = Networks.mac();

            // Jvm 分析的系统信息
            rt.javaVersion = System.getProperty("java.version");
            rt.osArch = System.getProperty("os.arch");
            rt.osName = System.getProperty("os.name").replace(" ", "");
            rt.osType = System.getProperty("sun.desktop");
            rt.osVersion = System.getProperty("os.version");
            rt.userCountry = System.getProperty("user.country");
            rt.userLanguage = System.getProperty("user.language");
            rt.userName = System.getProperty("user.name");
            rt.userTimezone = System.getProperty("user.timezone");
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

}
