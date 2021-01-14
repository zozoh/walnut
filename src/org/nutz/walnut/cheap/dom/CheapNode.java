package org.nutz.walnut.cheap.dom;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

public abstract class CheapNode {

    protected CheapNodeType type;

    private int nodeIndex;

    private int elementIndex;

    CheapDocument doc;

    CheapNode parent;

    CheapNode prev;

    CheapNode next;

    LinkedList<CheapNode> children;

    protected NutBean props;

    protected CheapNode() {
        this.nodeIndex = 0;
        this.props = new NutMap();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinTree(sb, 0, "|  ");
        return sb.toString();
    }

    public String toMarkup() {
        StringBuilder sb = new StringBuilder();
        this.joinString(sb);
        return sb.toString();
    }

    public abstract void joinTree(StringBuilder sb, int depth, String tab);

    public abstract void joinString(StringBuilder sb);

    /**
     * @param tab
     *            缩进字符
     * @param depth
     *            深度（根-1开始）
     * @param newLineTag
     *            是否为换行标签
     */
    public abstract void format(String tab, int depth, Pattern newLineTag);

    public abstract boolean isEmpty();

    public void filterEmptyChildren() {
        if (this.hasChildren()) {
            for (CheapNode child : children) {
                child.filterEmptyChildren();
            }
        }
        this.children = doc.filterEmptyNodes(children);
    }

    public boolean isRoot() {
        return null != doc && doc.root() == this;
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
        return this.doc.body() == this;
    }

    public boolean isRootElement() {
        return this.doc.root() == this;
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
        if (null == nodes || nodes.length == 0)
            return;

        // 初始化自己的子列表
        if (null == children)
            children = new LinkedList<>();

        // 自己直接就是没有子节点，那么直接设置
        if (children.isEmpty()) {
            // 编制节点互相连接
            CheapNode last = null;
            for (CheapNode node : nodes) {
                node.setParent(this);
                node.prev = last;
                if (null != last) {
                    last.next = node;
                }
                children.add(node);
                last = node;
            }
        }
        // 否则找到目标节点
        else {
            // 插入到末尾
            if (index == -1) {
                CheapNode last = this.children.getLast();
                last.insertNext(nodes);
            }
            // 插入到某节点前面
            else {
                // 确定位置
                if (index < 0) {
                    index = Math.max(0, children.size() + index + 1);
                }
                CheapNode node = children.get(index);
                node.insertPrev(nodes);
            }
        }
    }

    public void insertPrev(CheapNode... nodes) {
        if (null == nodes || nodes.length == 0)
            return;

        // 自己的兄弟链位置
        CheapNode c0 = prev; // 这个节点之后
        CheapNode c1 = this; // 这个节点之前

        // 插入兄弟链
        __insert_at(c0, c1, nodes);

        // 重新编制自己的索引
        int ndIx = this.getNodeIndex();
        nodes[0].resetIndex(ndIx);

        // 重新更新父的子节点
        if (null != this.parent) {
            CheapNode frs = this.getFirstSibling();
            this.parent.children = frs.getNextSiblings();
            this.parent.children.addFirst(frs);
        }
    }

    public void insertNext(CheapNode... nodes) {
        if (null == nodes || nodes.length == 0)
            return;

        // 自己的兄弟链位置
        CheapNode c0 = this; // 这个节点之后
        CheapNode c1 = next; // 这个节点之前

        // 插入兄弟链
        __insert_at(c0, c1, nodes);

        // 重新编制自己的索引
        int ndIx = this.getNodeIndex() + 1;
        nodes[0].resetIndex(ndIx);

        // 重新更新父的子节点
        if (null != this.parent) {
            CheapNode frs = this.getFirstSibling();
            this.parent.children = frs.getNextSiblings();
            this.parent.children.addFirst(frs);
        }
    }

    /**
     * @param c0
     *            这个节点之后
     * @param c1
     *            这个节点之前
     * @param nodes
     *            插入的节点列表
     */
    private void __insert_at(CheapNode c0, CheapNode c1, CheapNode... nodes) {
        // 编制节点互相连接
        CheapNode last = null;
        for (CheapNode node : nodes) {
            node.setParent(this.parent);
            node.prev = last;
            if (null != last) {
                last.next = node;
            }
            last = node;
        }

        // 确定链表首尾元素，以便之后记入兄弟链表
        CheapNode nd0 = nodes[0];
        CheapNode nd9 = nodes[nodes.length - 1];

        // 首
        if (null != c0) {
            c0.next = nd0;
            nd0.prev = c0;
        }
        // 尾
        if (null != c1) {
            c1.prev = nd9;
            nd9.next = c1;
        }
    }

    public CheapNode removeChild(int index) {
        CheapNode node = this.children.remove(index);
        __reset_sibling(node);
        return node;
    }

    public CheapNode removeFirstChild() {
        CheapNode node = this.children.removeFirst();
        __reset_sibling(node);
        return node;
    }

    public CheapNode removeLastChild() {
        CheapNode node = this.children.removeLast();
        __reset_sibling(node);
        return node;
    }

    private void __reset_sibling(CheapNode node) {
        CheapNode nd_prev = node.prev;
        CheapNode nd_next = node.next;
        if (null != nd_prev) {
            nd_prev.next = nd_next;
        }
        if (null != nd_next) {
            nd_next.prev = nd_prev;
        }
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
        return doc;
    }

    public List<CheapElement> getAncestors() {
        return getAncestors(null);
    }

    /**
     * @param tagName
     *            标签名（大小写不敏感）
     * @return 距离当前标签最近的祖先节点
     */
    public CheapElement getClosest(String tagName) {
        if (null == tagName) {
            return (CheapElement) this.parent;
        }
        tagName = tagName.toUpperCase();
        CheapElement $p = (CheapElement) this.parent;
        while (null != $p) {
            if (null == tagName || $p.isTagName(tagName)) {
                return $p;
            }
            $p = (CheapElement) $p.parent;
        }
        return null;
    }

    /**
     * 获取自己祖先元素列表
     * 
     * @param tagName
     *            过滤祖先标签，null 表示全部祖先
     * @return 祖先列表, 0 为自己最近的祖先。 <br>
     *         文档根元素，返回的是空列表，而不是<code>null</code>
     */
    public List<CheapElement> getAncestors(String tagName) {
        List<CheapElement> ans = new LinkedList<>();
        if (null != tagName) {
            tagName = tagName.toUpperCase();
        }
        CheapElement $p = (CheapElement) this.parent;
        while (null != $p) {
            if (null == tagName || $p.isTagName(tagName)) {
                ans.add($p);
            }
            $p = (CheapElement) $p.parent;
        }

        return ans;
    }

    public boolean hasParent() {
        return null != parent;
    }

    public CheapNode getParent() {
        return parent;
    }

    void setParent(CheapNode pnode) {
        this.parent = pnode;
        this.doc = pnode.doc;
    }

    public boolean hasPrevSibling() {
        return null != prev;
    }

    public CheapNode getPrevSibling() {
        return prev;
    }

    public LinkedList<CheapNode> getPrevSiblings() {
        LinkedList<CheapNode> list = new LinkedList<>();
        CheapNode $node = this.prev;
        while (null != $node) {
            list.add($node);
            $node = $node.prev;
        }
        return list;
    }

    public boolean hasNextSibling() {
        return null != next;
    }

    public CheapNode getNextSibling() {
        return next;
    }

    public LinkedList<CheapNode> getNextSiblings() {
        LinkedList<CheapNode> list = new LinkedList<>();
        CheapNode $node = this.next;
        while (null != $node) {
            list.add($node);
            $node = $node.next;
        }
        return list;
    }

    public CheapNode getFirstSibling() {
        CheapNode $node = this;
        while (null != $node && null != $node.prev) {
            $node = $node.prev;
        }
        return $node;
    }

    public CheapNode getLastSibling() {
        CheapNode $node = this;
        while (null != $node && null != $node.next) {
            $node = $node.next;
        }
        return $node;
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

    public void rebuildChildrenIndex() {
        if (this.hasChildren()) {
            int nIx = 0;
            int eIx = 0;
            for (CheapNode child : this.children) {
                child.setNodeIndex(nIx++);
                if (child.isElement()) {
                    child.setElementIndex(eIx++);
                }
                child.rebuildChildrenIndex();
            }
        }
    }

    protected void resetIndex(int start) {
        CheapNode $nd = this;
        while (null != $nd) {
            $nd.setNodeIndex(start++);
            $nd = $nd.next;
        }
    }

    public CheapNode propClear() {
        props.clear();
        return this;
    }

    public Set<String> propNames() {
        return props.keySet();
    }

    public Set<Map.Entry<String, Object>> propEntrySet() {
        return props.entrySet();
    }

    public boolean hasAttr(String name) {
        return props.has(name);
    }

    public boolean isProp(String name, Object val) {
        return props.is(name, val);
    }

    public CheapNode prop(String name, Object val) {
        props.put(name, val);
        return this;
    }

    public CheapNode props(Map<String, Object> bean) {
        if (null != bean) {
            props.putAll(bean);
        }
        return this;
    }

    public String prop(String name) {
        return props.getString(name);
    }

    public String propString(String name, String dft) {
        return props.getString(name, dft);
    }

    public int propInt(String name) {
        return props.getInt(name);
    }

    public int propInt(String name, int dft) {
        return props.getInt(name, dft);
    }

    public long propLong(String name) {
        return props.getLong(name);
    }

    public long propLong(String name, long dft) {
        return props.getLong(name, dft);
    }

    public boolean propBoolean(String name) {
        return props.getBoolean(name);
    }

    public boolean propBoolean(String name, boolean dft) {
        return props.getBoolean(name, dft);
    }

    public double propDouble(String name) {
        return props.getDouble(name);
    }

    public double propDouble(String name, double dft) {
        return props.getDouble(name, dft);
    }

    public float propFloat(String name) {
        return props.getFloat(name);
    }

    public float propFloat(String name, float dft) {
        return props.getFloat(name, dft);
    }

    public <T extends Enum<T>> T propEnum(String name, Class<T> classOfEnum) {
        return props.getEnum(name, classOfEnum);
    }

    public boolean propIsEnum(String name, Enum<?>... eus) {
        return props.isEnum(name, eus);
    }
}
