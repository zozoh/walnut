package org.nutz.walnut.cheap.ds;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.nutz.castor.Castors;
import org.nutz.lang.Strings;

public class AdjacencyNode {

    private int index;

    private Object data;

    private List<AdjacencyEdge> edges;

    private AdjacencyList list;

    AdjacencyNode(AdjacencyList list) {
        this.list = list;
        this.edges = new LinkedArrayList<>(AdjacencyEdge.class);
    }

    public String toString() {
        String dnm = this.getDataName();
        StringBuilder sb = new StringBuilder(String.format("Node[%d:%s]", index, dnm));
        for (AdjacencyEdge edge : edges) {
            sb.append(edge.toString());
        }
        return sb.toString();
    }

    String getDataName() {
        if (null == this.data)
            return "nil";

        String str = this.data.toString();
        if (str.length() > 3) {
            return str.substring(0, 3);
        }
        return Strings.alignLeft(str, 3, ' ');
    }

    public AdjacencyNode clone() {
        AdjacencyNode node = new AdjacencyNode(this.list);
        node.index = this.index;
        node.data = this.data;
        node.edges = new LinkedArrayList<>(AdjacencyEdge.class);
        Iterator<AdjacencyEdge> it = edges.iterator();
        while (it.hasNext()) {
            AdjacencyEdge ae = it.next();
            node.edges.add(ae.clone());
        }
        return node;
    }

    public boolean equals(Object node) {
        if (null == node || null == this.data)
            return false;

        if (node instanceof AdjacencyNode) {
            Object nd = ((AdjacencyNode) data).data;
            return this.data.equals(nd);
        }

        return this.data.equals(node);
        // return false;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public <T> T getDataAs(Class<T> classOfT) {
        return Castors.me().castTo(data, classOfT);
    }

    public boolean hasData() {
        return null != data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    /**
     * 增加一个边。需要保证不能重复。
     * 
     * @param nodeIndex
     *            节点 ID
     * @param weight
     *            权重
     */
    public void addEdge(int nodeIndex, int weight) {
        AdjacencyEdge edge = new AdjacencyEdge(list, nodeIndex, weight);
        int index = edges.indexOf(edge);
        // 修改
        if (index >= 0) {
            edges.set(index, edge);
        }
        // 追加
        else {
            edges.add(edge);
        }
    }

    public List<AdjacencyNode> getEdgeNodes() {
        List<AdjacencyNode> nodes = new ArrayList<>(this.edges.size());
        for (AdjacencyEdge edge : this.edges) {
            AdjacencyNode node = this.list.getNode(edge.getNodeIndex());
            if (null != node) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] getEdgeNodeDatas(Class<T> classOfT) {
        T[] res = (T[]) Array.newInstance(classOfT, edges.size());
        int i = 0;
        for (AdjacencyEdge edge : this.edges) {
            AdjacencyNode node = this.list.getNode(edge.getNodeIndex());
            if (null != node) {
                T d = node.getDataAs(classOfT);
                res[i] = d;
            }
            i++;
        }
        return res;
    }

    public List<AdjacencyEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<AdjacencyEdge> edges) {
        this.edges = edges;
    }

}
