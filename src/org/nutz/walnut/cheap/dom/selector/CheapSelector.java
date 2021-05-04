package org.nutz.walnut.cheap.dom.selector;

import java.util.List;

import org.nutz.walnut.cheap.dom.CheapElement;

public interface CheapSelector {

    /**
     * 从起始元素开始，把符合选择器的元素添加到结果列表
     * 
     * @param list
     *            结果列表
     * @param el
     *            起始元素
     * @param limit
     *            最多加入多少元素
     * 
     * @return 实际加入了多少元素
     */
    int join(List<CheapElement> list, CheapElement el, int limit);

    /**
     * 判断任何一个节点是否匹配指定选择器
     * 
     * @param el
     *            传入 DOM 的任意一个节点
     * @return 节点是否能匹配选择器
     */
    boolean match(CheapElement el);

    void joinString(StringBuilder sb);

    CheapSelector valueOf(String input);

    String toString();
}
