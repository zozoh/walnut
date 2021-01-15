package org.nutz.walnut.cheap.dom;

public interface CheapFilter {

    /**
     * @param el
     *            输入元素节点
     * @return true 表示匹配。 false 表示不匹配
     */
    boolean match(CheapElement el);

}
