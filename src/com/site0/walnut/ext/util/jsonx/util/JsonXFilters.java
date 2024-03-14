package com.site0.walnut.ext.util.jsonx.util;

import java.util.List;

import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.validate.WnMatch;

public abstract class JsonXFilters {

    /**
     * @param sys
     *            系统运行时
     * @param params
     *            参数表
     * @return 自动判断应该准备<b>自过滤</b>还是<b>它过滤</b>
     */
    public static WnMatch prepareMatch(WnSystem sys, ZParams params) {
        // 自过滤
        if (params.has("self")) {
            return new JsonXFilterSelfMatch(sys, params);
        }
        // 它过滤
        return new JsonXFilterGivenMatch(sys, params);
    }

    /**
     * @param input
     *            输入对象
     * @param wms
     *            匹配器列表
     * @param isAnd
     *            匹配器是否为<b>与</b>的关系
     * @return 输入对象是否能匹配匹配器列表
     */
    public static boolean match(Object input, List<WnMatch> wms, boolean isAnd) {
        // 必须全部通过
        if (isAnd) {
            for (WnMatch wm : wms) {
                if (!wm.match(input)) {
                    return false;
                }
            }
            return true;
        }
        // 只要一个通过就好
        for (WnMatch wm : wms) {
            if (wm.match(input)) {
                return true;
            }
        }
        return false;
    }

}
