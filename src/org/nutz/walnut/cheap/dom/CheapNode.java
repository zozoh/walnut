package org.nutz.walnut.cheap.dom;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Ws;

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

    @Override
    public CheapNode clone() {
        return this.cloneNode();
    }

    public abstract CheapNode cloneNode();

    public abstract void decodeEntities();

    @Override
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

    public String toInnerMarkup() {
        StringBuilder sb = new StringBuilder();
        if (this.hasChildren()) {
            for (CheapNode child : this.children) {
                child.joinString(sb);
            }
        }
        return sb.toString();
    }

    public abstract String toBrief();

    public abstract void joinTree(StringBuilder sb, int depth, String tab);

    public abstract void joinString(StringBuilder sb);

    public abstract void joinText(StringBuilder sb);

    public abstract void compact();

    /**
     * 如果未声明过滤器，或者过滤器返回真(True)，那么本函数行为与 compact 一致。 否则会导致跳过本节点的 compact 操作。
     * <p>
     * 这个函数对于仅仅希望压缩特定节点非常有用。譬如 OOML 里的 xml:space="preserve"， 明确指明了本节点要保留空格。<br>
     * 通过这个函数就比较容易做到仅仅压缩那些对空格不敏感的文本节点
     * 
     * @param flt
     *            过滤器
     */
    public abstract void compactWith(CheapNodeFilter flt);

    public int getAsInt() {
        String str = Ws.trim(this.getText());
        return Integer.parseInt(str);
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        joinText(sb);
        return sb.toString();
    }

    public abstract void setText(String text);

    /**
     * @param cdf
     *            格式化设置
     * @param depth
     *            深度（根-1开始）
     */
    public abstract void format(CheapFormatter cdf, int depth);

    /**
     * @return 当前节点是否没有任何内容
     */
    public abstract boolean isEmpty();

    /**
     * @return 当前节点是否没有任何可以显示的内容
     */
    public abstract boolean isBlank();

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

    public boolean isType(CheapNodeType type) {
        return this.type == type;
    }

    public boolean isSameType(CheapNode node) {
        return this.type == node.type;
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

    public <T extends CheapNode> CheapNode appendChildren(List<T> nodes) {
        if (null != nodes) {
            CheapNode[] children = new CheapNode[nodes.size()];
            nodes.toArray(children);
            this.append(children);
        }
        return this;
    }

    public CheapNode prepend(CheapNode... nodes) {
        this.add(0, nodes);
        return this;

    }

    public <T extends CheapNode> CheapNode prependChildren(List<T> nodes) {
        if (null != nodes) {
            CheapNode[] children = new CheapNode[nodes.size()];
            nodes.toArray(children);
            this.prepend(children);
        }
        return this;
    }

    public <T extends CheapNode> void setChildren(List<T> nodes) {
        this.children = new LinkedList<>();
        this.appendChildren(nodes);
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

    public <T extends CheapNode> CheapNode insertPrevNodes(List<T> nodes) {
        if (null != nodes) {
            CheapNode[] ary = new CheapNode[nodes.size()];
            nodes.toArray(ary);
            this.insertPrev(ary);
        }
        return this;
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

    public <T extends CheapNode> CheapNode insertNextNodes(List<T> nodes) {
        if (null != nodes) {
            CheapNode[] ary = new CheapNode[nodes.size()];
            nodes.toArray(ary);
            this.insertNext(ary);
        }
        return this;
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
            this.parent.children = frs.getNextSiblingsAndSelf();
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

    public void remove() {
        // 找到自己兄弟链的首节点
        CheapNode first = this.getFirstSibling();
        // 确保这个首节点在移除之后，是一定存在兄弟链中的
        if (first == this) {
            first = this.next;
        }

        // 将自己的前后兄弟握手
        if (null != prev) {
            prev.next = next;
        }
        if (null != next) {
            next.prev = prev;
            // 重新编制索引
            next.resetIndex(this.getNodeIndex());
        }

        // 更新自己的父节点
        if (null != first && null != parent) {
            parent.children = first.getNextSiblingsAndSelf();
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
        return getAncestorsByTagName(null);
    }

    /**
     * 根据标签名称寻找一个最近的祖先节点
     * 
     * @param tagName
     *            标签名（大小写不敏感）
     * @return 距离当前标签最近的祖先节点
     */
    public CheapElement getClosestByTagName(String tagName) {
        if (null == tagName) {
            return (CheapElement) this.parent;
        }
        tagName = tagName.toUpperCase();
        CheapElement $p = (CheapElement) this.parent;
        while (null != $p) {
            if ($p.isStdTagName(tagName)) {
                return $p;
            }
            $p = (CheapElement) $p.parent;
        }
        return null;
    }

    /**
     * 寻找一个最近的祖先节点
     * 
     * @param flt
     *            过滤器
     * @return 返回距离当前节点最近的，第一个被过滤器匹配的祖先节点
     */
    public CheapElement getClosest(CheapFilter flt) {
        if (null == flt) {
            return (CheapElement) this.parent;
        }
        CheapElement $p = (CheapElement) this.parent;
        while (null != $p) {
            if (flt.match($p)) {
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
    public List<CheapElement> getAncestorsByTagName(String tagName) {
        List<CheapElement> ans = new LinkedList<>();
        if (null != tagName) {
            tagName = tagName.toUpperCase();
        }
        CheapElement $p = (CheapElement) this.parent;
        while (null != $p) {
            if (null == tagName || $p.isStdTagName(tagName)) {
                ans.add($p);
            }
            $p = (CheapElement) $p.parent;
        }

        return ans;
    }

    /**
     * 获取符合匹配规则的祖先元素列表
     * 
     * @param ma
     *            匹配规则
     * @return 返回距离当前节点最近的，匹配规则的祖先节点。 0 为自己最近的祖先。 <br>
     *         文档根元素，返回的是空列表，而不是<code>null</code>
     */
    public List<CheapElement> getAncestors(CheapFilter ma) {
        List<CheapElement> ans = new LinkedList<>();
        CheapElement $p = (CheapElement) this.parent;
        while (null != $p) {
            if (null == ma || ma.match($p)) {
                ans.add($p);
            }
            $p = (CheapElement) $p.parent;
        }
        return ans;
    }

    /**
     * @param filter
     *            过滤器
     * @return 第一个符合过滤器的元素节点。 null 表示啥也没找到
     */
    public CheapElement findElement(CheapFilter filter) {
        return findElement(filter, null);
    }

    /**
     * @param filter
     *            过滤器
     * @param walkChildren
     *            当前节点是否继续递归子节点
     * @return 第一个符合过滤器的元素节点。 null 表示啥也没找到
     */
    public CheapElement findElement(CheapFilter filter, CheapFilter walkChildren) {
        if (null == filter || !this.isElement()) {
            return null;
        }
        CheapElement el = (CheapElement) this;
        if (filter.match(el)) {
            return el;
        }
        // 深度优先的便利
        if (null != this.children) {
            if (null == walkChildren || walkChildren.match(el))
                for (CheapNode node : this.children) {
                    CheapElement re = node.findElement(filter);
                    if (null != re)
                        return re;
                }
        }
        // 那就是啥都木有找到咯
        return null;
    }

    /**
     * @param filter
     *            过滤器
     * @return 符合条件的元素列表
     */
    public List<CheapElement> findElements(CheapFilter filter) {
        List<CheapElement> list = new LinkedList<>();
        joinElements(list, filter);
        return list;
    }

    /**
     * @param list
     *            输出列表
     * @param filter
     *            过滤器
     */
    public void joinElements(List<CheapElement> list, CheapFilter filter) {
        if (null == filter || !this.isElement()) {
            return;
        }
        if (filter.match((CheapElement) this)) {
            list.add((CheapElement) this);
        }
        // 深度优先的便利
        if (null != this.children) {
            for (CheapNode node : this.children) {
                node.joinElements(list, filter);
            }
        }
    }

    /**
     * 深度遍历子节点，如果过滤器返回 false，则不会递归下去
     * 
     * @param filter
     *            过滤器
     */
    public void walkElements(CheapFilter filter) {
        if (null == filter || !this.isElement()) {
            return;
        }
        if (filter.match((CheapElement) this)) {
            if (null != this.children) {
                for (CheapNode node : this.children) {
                    node.walkElements(filter);
                }
            }
        }
    }

    /**
     * @param filter
     *            过滤器
     * @return 第一个符合过滤器的节点。 null 表示啥也没找到
     */
    public CheapNode findNode(CheapNodeFilter filter) {
        if (null == filter) {
            return null;
        }
        if (filter.match(this)) {
            return this;
        }
        // 深度优先的便利
        if (null != this.children) {
            for (CheapNode node : this.children) {
                CheapNode re = node.findNode(filter);
                if (null != re)
                    return re;
            }
        }
        // 那就是啥都木有找到咯
        return null;
    }

    /**
     * @param filter
     *            过滤器
     * @return 符合条件的节点列表
     */
    public List<CheapNode> findNodes(CheapNodeFilter filter) {
        List<CheapNode> list = new LinkedList<>();
        joinNodes(list, filter);
        return list;
    }

    /**
     * @param list
     *            输出列表
     * @param filter
     *            过滤器
     */
    public void joinNodes(List<CheapNode> list, CheapNodeFilter filter) {
        if (null == filter) {
            return;
        }
        if (filter.match(this)) {
            list.add(this);
        }
        // 深度优先的便利
        if (null != this.children) {
            for (CheapNode node : this.children) {
                node.joinNodes(list, filter);
            }
        }
    }

    /**
     * @param callback
     *            回调， 如果返回 false 则停止继续深度遍历
     */
    public void eachNode(CheapNodeFilter callback) {
        if (null == callback) {
            return;
        }
        if (callback.match(this)) {
            // 深度优先的遍历
            if (null != this.children) {
                for (CheapNode node : this.children) {
                    node.eachNode(callback);
                }
            }
        }
    }

    public boolean hasParent() {
        return null != parent;
    }

    public CheapNode getParent() {
        return parent;
    }

    void setParent(CheapNode pnode) {
        this.parent = pnode;
        this.setOwnerDocument(pnode.doc);
    }

    void setOwnerDocument(CheapDocument doc) {
        // 之前没有设置过所属文档
        if (this.doc != doc) {
            this.doc = doc;

            // 递归搞一下
            if (this.hasChildren()) {
                for (CheapNode child : this.children) {
                    child.setOwnerDocument(doc);
                }
            }
        }
    }

    public boolean hasPrevSibling() {
        return null != prev;
    }

    public CheapNode getPrevSibling() {
        return prev;
    }

    public CheapElement getPrevElement() {
        return this.getPrevElement(null);
    }

    public CheapElement getPrevElementByName(String tagName) {
        if (null == tagName) {
            return this.getPrevElement(null);
        }
        String upperName = tagName.toUpperCase();
        return this.getPrevElement(el -> el.isStdTagName(upperName));
    }

    public CheapElement getPrevElement(CheapFilter flt) {
        CheapNode n2 = this.prev;
        while (n2 != null) {
            if (n2.isElement()) {
                CheapElement el = (CheapElement) n2;
                if (null == flt || flt.match(el)) {
                    return el;
                }
            }
            n2 = n2.prev;
        }
        return null;
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

    public CheapElement getNextElement() {
        return this.getNextElement(null);
    }

    public CheapElement getNextElementByName(String tagName) {
        if (null == tagName) {
            return this.getNextElement(null);
        }
        String upperName = tagName.toUpperCase();
        return this.getNextElement(el -> el.isStdTagName(upperName));
    }

    public CheapElement getNextElement(CheapFilter flt) {
        CheapNode n2 = this.next;
        while (n2 != null) {
            if (n2.isElement()) {
                CheapElement el = (CheapElement) n2;
                if (null == flt || flt.match(el)) {
                    return el;
                }
            }
            n2 = n2.next;
        }
        return null;
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

    public LinkedList<CheapNode> getNextSiblingsAndSelf() {
        LinkedList<CheapNode> list = new LinkedList<>();
        list.add(this);
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

    public boolean hasElements() {
        return hasElements(null);
    }

    public boolean hasElements(CheapFilter flt) {
        if (null != children) {
            for (CheapNode child : children) {
                if (!child.isElement()) {
                    continue;
                }
                if (null == flt || flt.match((CheapElement) child)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int countChildren() {
        if (null == children)
            return 0;
        return children.size();
    }

    public int countChildElements(CheapFilter flt) {
        int re = 0;
        if (null != children) {
            for (CheapNode child : children) {
                if (!child.isElement()) {
                    continue;
                }
                if (null == flt || flt.match((CheapElement) child)) {
                    re++;
                }
            }
        }
        return re;
    }

    public List<CheapNode> getChildren() {
        return children;
    }

    public List<CheapElement> getChildElements() {
        return this.getChildElements(null);
    }

    public List<CheapElement> getChildElements(CheapFilter flt) {
        List<CheapElement> list = new LinkedList<>();
        if (this.hasChildren()) {
            for (CheapNode child : children) {
                if (!child.isElement()) {
                    continue;
                }
                if (null == flt || flt.match((CheapElement) child)) {
                    list.add((CheapElement) child);
                }
            }
        }
        return list;
    }

    public CheapNode getFirstChild() {
        return getFirstChild(null);
    }

    public CheapNode getFirstNoBlankChild() {
        return getFirstChild(node -> !node.isBlank());
    }

    public CheapNode getFirstChild(CheapNodeFilter flt) {
        CheapNode node = null;
        if (this.hasChildren()) {
            node = children.getFirst();
            if (null != flt) {
                while (null != node && !flt.match(node)) {
                    node = node.next;
                }
            }
        }
        return node;
    }

    public CheapNode getLastChild() {
        return getLastChild(null);
    }

    public CheapNode getLastNoBlankChild() {
        return getLastChild(node -> !node.isBlank());
    }

    public CheapNode getLastChild(CheapNodeFilter flt) {
        CheapNode node = null;
        if (this.hasChildren()) {
            node = children.getLast();
            if (null != flt) {
                while (null != node && !flt.match(node)) {
                    node = node.prev;
                }
            }
        }
        return node;
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

    public boolean hasProp(String name) {
        return props.has(name);
    }

    public CheapNode setPlacehold(boolean val) {
        this.prop("CheapPlacehold", val);
        return this;
    }

    public boolean isPlacehold() {
        return this.isProp("CheapPlacehold", true);
    }

    public CheapNode setFormatted(boolean val) {
        this.prop("CheapFormated", val);
        return this;
    }

    public boolean isFormated() {
        return this.isProp("CheapFormated", true);
    }

    public boolean hasProps() {
        return null != props && !props.isEmpty();
    }

    public boolean isProp(String name, Object val) {
        return props.is(name, val);
    }

    public CheapNode prop(String name, Object val) {
        props.put(name, val);
        return this;
    }

    public CheapNode propsPutAll(Map<String, Object> bean) {
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
