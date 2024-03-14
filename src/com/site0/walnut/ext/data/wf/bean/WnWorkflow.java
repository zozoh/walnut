package com.site0.walnut.ext.data.wf.bean;

import java.util.LinkedHashMap;
import java.util.Map;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.Wlang;

public class WnWorkflow {

    private Map<String, WfNode> nodes;

    private Map<String, Map<String, WfEdge>> edges;

    @SuppressWarnings("unchecked")
    public WnWorkflow(NutMap map) {
        // 解析节点
        this.nodes = new LinkedHashMap<>();
        NutMap nodes = map.getAs("nodes", NutMap.class);
        if (null != nodes) {
            for (Map.Entry<String, Object> en : nodes.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                if (null == val) {
                    continue;
                }
                if (val instanceof Map<?, ?>) {
                    NutMap m2 = NutMap.WRAP((Map<String, Object>) val);
                    WfNode wfNode = Wlang.map2Object(m2, WfNode.class);
                    this.nodes.put(key, wfNode);
                }
            }
        }
        // 解析边
        this.edges = new LinkedHashMap<>();
        NutMap edges = map.getAs("edges", NutMap.class);
        if (null != edges) {
            for (Map.Entry<String, Object> en : edges.entrySet()) {
                String from = en.getKey();
                Object val = en.getValue();
                if (null == val) {
                    continue;
                }

                Map<String, WfEdge> edgeSet = new LinkedHashMap<>();
                this.edges.put(from, edgeSet);

                if (val instanceof Map<?, ?>) {
                    NutMap m2 = NutMap.WRAP((Map<String, Object>) val);
                    for (Map.Entry<String, Object> en2 : m2.entrySet()) {
                        String to = en2.getKey();
                        Object v2 = en2.getValue();
                        if (null == v2) {
                            continue;
                        }
                        if (v2 instanceof Map<?, ?>) {
                            NutMap m3 = NutMap.WRAP((Map<String, Object>) v2);
                            WfEdge wfEdge = Wlang.map2Object(m3, WfEdge.class);

                            edgeSet.put(to, wfEdge);
                        }
                    }
                }
            }
        }
    }

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

    public String findStartNodeName() {
        for (Map.Entry<String, WfNode> en : nodes.entrySet()) {
            WfNode node = en.getValue();
            if (node.isHEAD()) {
                return en.getKey();
            }
        }
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
