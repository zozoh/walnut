package org.nutz.walnut.ext.data.wf.bean;

import java.util.Map;

import org.nutz.lang.util.NutMap;

public class WnWorkflow {

    private Map<String, WfNode> nodes;

    private Map<String, Map<String, WfEdge>> edges;

    public WfEdge tryEdge(String fromName, NutMap vars) {
        // 找到当前节点的边集合
        Map<String, WfEdge> edgeSet = this.edges.get(fromName);

        // 防守
        if (null == edgeSet || edgeSet.isEmpty()) {
            return null;
        }

        // 逐个判断边是否可以被连通
        for (Map.Entry<String, WfEdge> en : edgeSet.entrySet()) {
            WfEdge edge = en.getValue();
            // 确保边设置 from -> to
            String toName = en.getKey();
            edge.setEdgeName(fromName, toName);
            // 判断
            if (edge.isOn(vars)) {
                return edge;
            }
        }

        // 返回 null 表示没有找到连通的边
        return null;
    }

    public Map<String, WfNode> getNodes() {
        return nodes;
    }

    public WfNode getNode(String name) {
        return nodes.get(name);
    }

    public WfEdge getEdge(String fromName, String toName) {
        Map<String, WfEdge> nodes = edges.get(fromName);
        if (null != nodes)
            return nodes.get(toName);
        return null;
    }

    public void setNodes(Map<String, WfNode> nodes) {
        this.nodes = nodes;
    }

    public Map<String, Map<String, WfEdge>> getEdges() {
        return edges;
    }

    public void setEdges(Map<String, Map<String, WfEdge>> edges) {
        this.edges = edges;
    }

}
