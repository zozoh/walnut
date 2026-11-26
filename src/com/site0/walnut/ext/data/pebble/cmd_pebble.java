package com.site0.walnut.ext.data.pebble;

import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;

/**
 * `pebble` 用 Pebble 模板引擎渲染页面模板：支持继承/包含/宏，默认 HTML 转义。
 *
 * <p>
 * 过滤器：`@load`（模板来源）、`@vars`（上下文变量）、`@render`（渲染输出）。
 * 用法见 man/zh_cn/cmd_pebble.md。
 */
public class cmd_pebble extends JvmFilterExecutor<PebbleContext, PebbleFilter> {

    public cmd_pebble() {
        super(PebbleContext.class, PebbleFilter.class);
    }

    @Override
    protected PebbleContext newContext() {
        return new PebbleContext();
    }

    @Override
    protected void prepare(WnSystem sys, PebbleContext fc) {
        // 什么也不需要准备
    }

    @Override
    protected void output(WnSystem sys, PebbleContext fc) {
        // 输出由 @render 过滤器完成
    }
}
