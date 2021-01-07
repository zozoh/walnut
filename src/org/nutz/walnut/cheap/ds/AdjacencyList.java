package org.nutz.walnut.cheap.ds;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

/**
 * 邻接表实现
 * <p>
 * 一个邻接表分作两个部分，一个是顶点列表，一个边矩阵
 * 
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AdjacencyList {

    private List<AdjacencyNode> nodes;

    public AdjacencyList() {
        this(50);
    }

    public AdjacencyList(int width) {
        this.nodes = new LinkedArrayList<>(AdjacencyNode.class, width);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (AdjacencyNode node : nodes) {
            sb.append(node.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public AdjacencyNode addNode(Object data) {
        AdjacencyNode newNode = new AdjacencyNode(this);
        newNode.setIndex(nodes.size());
        newNode.setData(data);
        nodes.add(newNode);
        return newNode;
    }

    public AdjacencyNode addNode(int index, Object data) {
        return addNode(index, data, 1);
    }

    public AdjacencyNode addNode(AdjacencyNode node, Object data) {
        return addNode(node, data, 1);
    }

    public AdjacencyNode addNode(int index, Object data, int weight) {
        AdjacencyNode node = nodes.get(index);
        return addNode(node, data, weight);
    }

    public AdjacencyNode addNode(AdjacencyNode node, Object data, int weight) {
        AdjacencyNode newNode = null;
        if (null != node) {
            newNode = addNode(data);
            node.addEdge(newNode.getIndex(), weight);
        }
        return newNode;
    }

    public <T> T getNodeData(int index, Class<T> classOfT) {
        AdjacencyNode node = nodes.get(index);
        if (null != node) {
            return node.getDataAs(classOfT);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] getNodeEdges(int index, Class<T> classOfT) {
        AdjacencyNode node = nodes.get(index);
        if (null != node) {
            return node.getEdgeNodeDatas(classOfT);
        }
        return (T[]) Array.newInstance(classOfT, 0);
    }

    public AdjacencyNode getNode(int index) {
        return nodes.get(index);
    }

    public int indexOf(Object obj) {
        return nodes.indexOf(obj);
    }

    public AdjacencyNode findNode(Object obj) {
        if (null == obj)
            return null;
        Iterator<AdjacencyNode> it = nodes.iterator();
        while (it.hasNext()) {
            AdjacencyNode node = it.next();
            if (node.equals(obj)) {
                return node;
            }
        }
        return null;
    }
}
