package org.nutz.walnut.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import org.nutz.walnut.api.err.Er;

public abstract class WnOS {

    public static void fillRuntimeInfo(WnSysRuntime rt) {
        try {
            // 本机信息
            InetAddress ia = InetAddress.getLocalHost();
            rt.machineName = ia.getHostName();
            rt.localAddress = ia.getHostAddress();
            rt.localMac = getlMacAddress(ia);

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

    /**
     * 获取本地网卡地址
     * 
     * @param ia
     * @return 本地网卡地址码，类似 <code>0B0028000020</code>
     * @throws SocketException
     */
    public static String getlMacAddress(InetAddress ia) throws SocketException {
        // 获取网卡，获取地址
        NetworkInterface addr = NetworkInterface.getByInetAddress(ia);
        byte[] mac = addr.getHardwareAddress();

        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < mac.length; i++) {
            // 字节转换为整数
            int temp = mac[i] & 0xff;
            String str = Integer.toHexString(temp);
            if (str.length() == 1) {
                sb.append("0" + str);
            } else {
                sb.append(str);
            }
        }
        // System.out.println("本机MAC地址:" + sb.toString().toUpperCase());
        return sb.toString().toUpperCase();
    }

}
