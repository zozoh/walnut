package org.nutz.walnut.cheap.dom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CheapNode {

    protected CheapNodeType type;

    private int nodeIndex;

    private int elementIndex;

    CheapDocument ownerDocument;

    private CheapNode parent;

    private LinkedList<CheapNode> children;

    protected CheapNode() {
        this.nodeIndex = 0;
    }

    public boolean isElement() {
        return CheapNodeType.ELEMENT == type;
    }

    public boolean isText() {
        return CheapNodeType.TEXT == type;
    }

    public boolean isComment() {
        return CheapNodeType.COMMENT == type;
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

    public boolean isBodyElement() {
        return this.ownerDocument.body() == this;
    }

    public boolean isRootElement() {
        return this.ownerDocument.root() == this;
    }

    public CheapNode appendTo(CheapNode pnode) {
        pnode.append(this);
        return this;
    }

    public CheapNode prependTo(CheapNode pnode) {
        pnode.prepend(this);
        return this;
    }

    public CheapNode append(CheapNode... nodes) {
        this.add(-1, nodes);
        return this;
    }

    public CheapNode prepend(CheapNode... nodes) {
        this.add(0, nodes);
        return this;
    }

    public void add(int index, CheapNode... nodes) {
        if (null == children) {
            children = new LinkedList<>();
        }
        List<CheapNode> list = new ArrayList<>(nodes.length);
        for (CheapNode node : nodes) {
            node.setParent(this);
            if (node.ownerDocument != this.ownerDocument) {
                node.ownerDocument = this.ownerDocument;
            }
        }
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

    public CheapNode removeChild(int index) {
        return this.children.remove(index);
    }

    public CheapNode removeFirstChild() {
        return this.children.removeFirst();
    }

    public CheapNode removeLastChild() {
        return this.children.removeLast();
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    void setNodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public int getElementIndex() {
        return elementIndex;
    }

    void setElementIndex(int elementIndex) {
        this.elementIndex = elementIndex;
    }

    public CheapDocument getOwnerDocument() {
        return ownerDocument;
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

    public CheapNode getFirstChild() {
        if (this.hasChildren()) {
            return children.getFirst();
        }
        return null;
    }

    public CheapNode getLastChild() {
        if (this.hasChildren()) {
            return children.getLast();
        }
        return null;
    }

    public CheapNode empty() {
        if (this.hasChildren()) {
            this.children.clear();
        }
        return this;
    }

    public void rebuildChildren() {
        if (this.hasChildren()) {
            int nIx = 0;
            int eIx = 0;
            for (CheapNode child : this.children) {
                child.setNodeIndex(nIx++);
                if (child.isElement()) {
                    child.setElementIndex(eIx++);
                }
                child.rebuildChildren();
            }
        }
    }

}
