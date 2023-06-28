package org.nutz.walnut.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.walnut.WnVersion;
import org.nutz.walnut.api.err.Er;

public abstract class WnOS {

    private static final Log log = Wlog.getMAIN();

    public static void fillRuntimeInfo(WnSysRuntime rt) {
        try {
            log.info("WnOS.fillRuntimeInfo >>>");
            InetAddress ia = null;
            // 本机信息
            try {
                ia = InetAddress.getLocalHost();
                if (null != ia) {
                    rt.machineName = ia.getHostName();
                }
            }
            catch (UnknownHostException e) {}

            // 用默认的网卡看看能不能取到全部的值
            NetworkInterface nif = null;
            if (null != ia) {
                nif = NetworkInterface.getByInetAddress(ia);
            }

            // 默认填充失败，则逐个寻找网卡，尝试填充
            if (null == nif || !_try_interface(rt, nif)) {
                fillRuntimeByNetworks(rt);
            }

            // Jvm 分析的系统信息
            log.info(" - load sys properties ...");
            rt.nodeVersion = WnVersion.getName();
            rt.nodeVersionNumber = WnVersion.get();
            rt.javaVersion = System.getProperty("java.version");
            rt.osArch = System.getProperty("os.arch");
            rt.osName = System.getProperty("os.name").replace(" ", "");
            rt.osType = System.getProperty("sun.desktop");
            rt.osVersion = System.getProperty("os.version");
            rt.userCountry = System.getProperty("user.country");
            rt.userLanguage = System.getProperty("user.language");
            rt.userName = System.getProperty("user.name");
            rt.userTimezone = System.getProperty("user.timezone");
            log.info("<<<");
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

    public static void fillRuntimeByNetworks(WnSysRuntime rt) throws SocketException {
        Enumeration<NetworkInterface> netIFs = getNetworkInterfaces();

        Stopwatch sw = Stopwatch.begin();
        sw.tag("getNetworkInterfaces");
        while (netIFs.hasMoreElements()) {
            NetworkInterface nif = netIFs.nextElement();
            if (_try_interface(rt, nif)) {
                break;
            }
        }

    }

    private static boolean _try_interface(WnSysRuntime rt, NetworkInterface nif)
            throws SocketException {
        byte[] hardAddr = null;
        String name = null;
        String disName = null;
        String ipv4 = null;

        // 必须有名称
        name = nif.getName();
        if (Strings.isBlank(name)) {
            throw Lang.makeThrow("Face without name", nif);
        }

        // 必须有硬件地址
        hardAddr = nif.getHardwareAddress();
        if (null == hardAddr) {
            return false;
        }

        // 必须有显示名称
        disName = nif.getDisplayName();
        if (Strings.isBlank(disName)) {
            return false;
        }

        // 必须有 IP
        Enumeration<InetAddress> ifAddrs = nif.getInetAddresses();
        if (null != ifAddrs) {
            while (ifAddrs.hasMoreElements()) {
                InetAddress ia = ifAddrs.nextElement();
                ipv4 = ia.getHostAddress();
                if (ipv4.matches("^[0-9.]+$")) {
                    hardAddr = ia.getAddress();
                    break;
                }
                ipv4 = null;
            }
        }
        if (null == ipv4) {
            return false;
        }

        // 那么就算是找到咯
        // 记录一下
        rt.netName = name;
        rt.netDisplayName = disName;
        rt.netIpv4 = ipv4;
        rt.netMac = getMacAddress(hardAddr);
        return true;
    }

    private static Enumeration<NetworkInterface> netIFs;

    public static Enumeration<NetworkInterface> getNetworkInterfaces() {
        if (null == netIFs) {
            try {
                netIFs = NetworkInterface.getNetworkInterfaces();
            }
            catch (SocketException e) {
                throw Lang.wrapThrow(e);
            }
        }
        return netIFs;
    }

    public static String getMacAddress(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data)
            sb.append(Strings.toHex(b, 2));
        return sb.toString().toUpperCase();
    }

}
