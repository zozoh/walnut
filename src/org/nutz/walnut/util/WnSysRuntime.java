package org.nutz.walnut.util;

/**
 * 系统运行时信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSysRuntime {

    private String nodeName;

    private long nodeStartAtInMs;

    private long nodeNowInMs;

    private long nodeLiveTimeInMs;

    public WnSysRuntime(String nodeName) {
        this.nodeName = nodeName;
        this.nodeStartAtInMs = System.currentTimeMillis();
    }

    public String getNodeName() {
        return nodeName;
    }

    public long getNodeStartAtInMs() {
        return nodeStartAtInMs;
    }

    public long getNodeNowInMs() {
        return nodeNowInMs;
    }

    public long getNodeLiveTimeInMs() {
        return nodeLiveTimeInMs;
    }

    public WnSysRuntime clone() {
        WnSysRuntime rt = new WnSysRuntime(this.nodeName);
        rt.nodeName = this.nodeName;
        rt.nodeStartAtInMs = this.nodeStartAtInMs;
        rt.nodeNowInMs = System.currentTimeMillis();
        rt.nodeLiveTimeInMs = rt.nodeNowInMs - rt.nodeStartAtInMs;
        return rt;
    }

}
