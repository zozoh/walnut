package org.nutz.walnut.alg.ds.adj;

public class AdjacencyEdge {

    private AdjacencyList list;

    private int nodeIndex;

    /**
     * 边权重
     */
    private int weight;

    /**
     * 边的可用次数 0 代表无限次
     */
    private int avaliable;

    /**
     * 边的实际使用次数
     */
    private int used;

    public AdjacencyEdge(AdjacencyList list, int nodeIndex) {
        this(list, nodeIndex, 1, 0);
    }

    public AdjacencyEdge(AdjacencyList list, int nodeIndex, int weight, int ava) {
        this.list = list;
        this.nodeIndex = nodeIndex;
        this.weight = weight;
        this.avaliable = ava;
        this.used = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;

        if (obj instanceof AdjacencyEdge) {
            AdjacencyEdge edge = (AdjacencyEdge) obj;
            return this.nodeIndex == edge.nodeIndex;
        }

        return false;
    }

    @Override
    protected AdjacencyEdge clone() {
        AdjacencyEdge edge = new AdjacencyEdge(list, nodeIndex, weight, avaliable);
        edge.used = this.used;
        return edge;
    }

    @Override
    public String toString() {
        AdjacencyNode node = list.getNode(nodeIndex);
        String nnm = "???";
        if (null != node) {
            if (node.hasData()) {
                nnm = node.getDataName();
            } else {
                nnm = "nil";
            }
        }
        if (weight == 1) {
            return String.format("---[%d:%s]", nodeIndex, nnm);
        }
        return String.format("-%d-[%d:%s]", weight, nodeIndex, nnm);
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getAvaliable() {
        return avaliable;
    }

    public void setAvaliable(int avaliable) {
        this.avaliable = avaliable;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

}
