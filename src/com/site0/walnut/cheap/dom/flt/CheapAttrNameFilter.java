package com.site0.walnut.cheap.dom.flt;

public interface CheapAttrNameFilter {

    /**
     * @param name
     *            属性名称
     * @return 如果返回 null表示未匹配上，返回不同的名字则表示改名称
     */
    String getName(String name);

}
