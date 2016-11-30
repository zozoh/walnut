package org.nutz.walnut.ext.hmaker.util;

import org.jsoup.nodes.Element;

public interface HmComHandler {

    /**
     * 执行转换
     * 
     * @param ing
     *            转换上下文
     */
    void invoke(HmPageTranslating ing);

    /**
     * @param eleCom
     *            控件对应的 DOM 节点
     * @return 控件是否是动态的
     */
    boolean isDynamic(Element eleCom);

}
