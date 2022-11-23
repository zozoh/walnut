package org.nutz.walnut.util;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;

/**
 * 系统运行时信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSysRuntime {

    /**
     * 系统版本
     */
    String nodeVersion;

    /**
     * 节点唯一名称
     */
    String nodeName;

    /**
     * 节点启动时间（唯一毫秒数）
     */
    long nodeStartAtInMs;

    /**
     * 本机机器名
     */
    String machineName;

    /**
     * 本机默认网卡名称
     */
    String netName;

    /**
     * 本机默认网卡显示名
     */
    String netDisplayName;

    /**
     * 本机默认网卡 IPv4 地址
     */
    String netIpv4;

    /**
     * 本机默认网卡硬件地址
     */
    String netMac;

    String javaVersion;
    String osArch;
    String osName;
    String osType;
    String osVersion;
    String userCountry;
    String userLanguage;
    String userName;
    String userTimezone;

    NutMap props;

    public WnSysRuntime(String nodeName) {
        WnOS.fillRuntimeInfo(this);

        // 默认拼合一个自己的节点的名称
        if (Strings.isBlank(nodeName)) {
            this.nodeName = this.machineName + "-" + this.netMac;
        }
        // 指定了节点
        else {
            NutMap context = this.toMap();
            this.nodeName = Tmpl.exec(nodeName, context);
        }

        this.nodeStartAtInMs = Wn.now();
        this.props = new NutMap();
    }

    public String getNodeVersion() {
        return nodeVersion;
    }

    public void setNodeVersion(String version) {
        this.nodeVersion = version;
    }

    public String getNodeName() {
        return nodeName;
    }

    public long getNodeStartAtInMs() {
        return nodeStartAtInMs;
    }

    public String getMachineName() {
        return machineName;
    }

    public String getNetName() {
        return netName;
    }

    public String getNetDisplayName() {
        return netDisplayName;
    }

    public String getNetIpv4() {
        return netIpv4;
    }

    public String getNetMac() {
        return netMac;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getOsArch() {
        return osArch;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsType() {
        return osType;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public String getUserLanguage() {
        return userLanguage;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserTimezone() {
        return userTimezone;
    }

    public NutMap props() {
        return this.props;
    }

    public NutMap toMap() {
        String json = Json.toJson(this);
        return Json.fromJson(NutMap.class, json);
    }

}
