package com.site0.walnut.api.io;

public interface WnObjFilter {

    /**
     * @param obj
     *            传入对象
     * @return true 匹配上了过滤器。 false 未匹配上过滤器
     */
    boolean match(WnObj obj);

}
