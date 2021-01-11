package org.nutz.walnut.cheap.dom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CheapNode {

    protected CheapNodeType type;

    private int nodeIndex;

    private CheapDocument ownerDocument;

    private CheapNode parent;

    private LinkedList<CheapNode> children;

    public CheapNode() {
        this.nodeIndex = 0;
    }

    public boolean isElement() {
        return CheapNodeType.ELEMENT == type;
    }

    public boolean isText() {
        return CheapNodeType.TEXT == type;
    }

    public boolean isRawData() {
        return CheapNodeType.RAW_DATA == type;
    }

    public CheapNodeType getType() {
        return type;
    }

    void setType(CheapNodeType type) {
        this.type = type;
    }

    public void appendTo(CheapNode pnode) {
        pnode.append(this);
        this.setParent(pnode);
    }

    public void prependTo(CheapNode pnode) {
        pnode.prepend(this);
        this.setParent(pnode);
    }

    public void append(CheapNode... nodes) {
        this.add(-1, nodes);
    }

    public void prepend(CheapNode... nodes) {
        this.add(0, nodes);
    }

    public void add(int index, CheapNode... nodes) {
        if (null == children) {
            children = new LinkedList<>();
        }
        List<CheapNode> list = new ArrayList<>(nodes.length);
        // 最后一个
        if (index == -1) {
            this.children.addAll(list);
        }
        // 在特殊位置插入
        else {
            // 倒数
            if (index < 0) {
                index = Math.max(0, children.size() + index);
            }
            this.children.addAll(index, list);
        }
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    void setNodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public CheapDocument getOwnerDocument() {
        return ownerDocument;
    }

    public void setOwnerDocument(CheapDocument doc) {
        this.ownerDocument = doc;
    }

    public boolean hasParent() {
        return null != parent;
    }

    public CheapNode getParent() {
        return parent;
    }

    public void setParent(CheapNode pnode) {
        this.parent = pnode;
    }

    public boolean hasChildren() {
        return null != children && !children.isEmpty();
    }

    public List<CheapNode> getChildren() {
        return children;
    }

    public void rebuildChildren() {
        int i = this.children.size();
        for (CheapNode child : this.children) {
            child.setNodeIndex(i++);
            child.setOwnerDocument(this.ownerDocument);
        }
    }

}
