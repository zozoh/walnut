package org.nutz.walnut.impl.box;

import org.nutz.walnut.util.ZParams;

public abstract class JvmFilter<C extends JvmFilterContext> {

    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, null);
    }

    /**
     * @param sys
     *            系统运行时
     * @param fc
     *            过滤器上下文
     * @param params
     *            当前过滤器参数
     */
    abstract protected void process(WnSystem sys, C fc, ZParams params);

}
