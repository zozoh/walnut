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

    /**
     * 根据当前控件的信息，获取控件的值（主要给 hmc_dynamic用）
     * 
     * @param eleCom 控件对应的元素
     * 
     * @return 控件的值
     */
    Object getValue(Element eleCom);

}
