package com.site0.walnut.util.obj;

import com.site0.walnut.api.io.WnObj;

public interface WnObjRenaming {

    /**
     * @param o
     *            传入对象
     * @return 新名称，<code>null</code>表示不需要重命名
     */
    String getName(WnObj o);

}
