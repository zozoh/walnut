package org.nutz.walnut.ext.hmaker.util;

import java.util.List;

import org.jsoup.nodes.Element;
import org.nutz.walnut.ext.hmaker.util.bean.HmcDynamicScriptInfo;

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
     * @param eleCom
     *            控件对应的元素
     * @param key
     *            控件要填充值的 key，对于复杂值，通常 key 为 `_` 开头，譬如 `_flt`<br>
     *            这个实现类通常应该将其融合进 si 里
     * @param hdsi
     *            要将值填充到的上下文
     */
    void loadValue(Element eleCom, String key, HmcDynamicScriptInfo hdsi);

    /**
     * 将自己内部锚点信息加入给定列表（不包括自己ID）
     * 
     * @param eleCom
     *            控件对应的元素
     * @param list
     *            要填充的列表
     */
    void joinAnchorList(Element eleCom, List<String> list);

    /**
     * 将自己内部动态数据加入给定列表
     * 
     * @param eleCom
     *            控件对应的元素
     * @param list
     *            要填充的列表
     */
    void joinParamList(Element eleCom, List<String> list);

}
