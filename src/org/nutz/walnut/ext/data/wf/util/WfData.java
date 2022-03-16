package org.nutz.walnut.ext.data.wf.util;

import java.util.Map;

public class WfData {

    private Map<String, WfNode> nodes;

    private Map<String, Map<String, WfEdge>> edges;

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
