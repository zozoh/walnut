package com.site0.walnut.util;

import com.site0.walnut.api.io.WnObj;

public interface WnObjWalkjFilter {

    /**
     * @param obj
     *            对象
     * @return false 表示不符合条件。 true 表示符合条件
     */
    boolean match(WnObj obj);

}
