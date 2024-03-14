package com.site0.walnut.cheap.dom;

public interface CheapNodeFilter {

    /**
     * @param node
     *            输入节点
     * @return true 表示匹配。 false 表示不匹配
     */
    boolean match(CheapNode node);

}
