package com.site0.walnut.ext.data.fake.impl;

public enum WnNameFakeMode {

    /**
     * 全名(姓+[中间]+名)
     * <p>
     * 随机出现中间名
     */
    FULL,

    /**
     * 全名(姓+中间+名)
     */
    FULL_WITH_MIDDLE,

    /**
     * 全名(姓+名)
     */
    FULL_NO_MIDDLE,

    /**
     * 仅名称
     */
    FIRST,

    /**
     * 仅中间名
     */
    MIDDLE,

    /**
     * 仅姓名
     */
    FAMILY
}
