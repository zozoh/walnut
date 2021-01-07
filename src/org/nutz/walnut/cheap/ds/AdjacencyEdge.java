package org.nutz.walnut.cheap.ds;

public class AdjacencyEdge {

    private AdjacencyList list;

    private int nodeIndex;

    private int weight;

    public AdjacencyEdge(AdjacencyList list, int nodeIndex) {
        this(list, nodeIndex, 1);
    }

    public AdjacencyEdge(AdjacencyList list, int nodeIndex, int weight) {
        this.list = list;
        this.nodeIndex = nodeIndex;
        this.weight = weight;
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
        return new AdjacencyEdge(list, nodeIndex, weight);
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

}
