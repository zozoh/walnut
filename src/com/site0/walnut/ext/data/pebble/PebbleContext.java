package com.site0.walnut.ext.data.pebble;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.impl.box.JvmFilterContext;

/**
 * `pebble` 命令的过滤器上下文。
 *
 * <p>
 * 模板来源二选一：内联源码 {@link #tmpl}，或对象树里的模板文件 {@link #tplPath}（绝对路径）。
 */
public class PebbleContext extends JvmFilterContext {

    /**
     * 模板源码（`@load 'xxx'` 或管道）
     */
    public String tmpl;

    /**
     * 模板文件路径，已归一为绝对路径（`@load -f ~/xxx`）
     */
    public String tplPath;

    /**
     * 渲染上下文变量
     */
    public NutMap vars = new NutMap();

    public void putVars(NutMap more) {
        if (null != more && !more.isEmpty()) {
            this.vars.putAll(more);
        }
    }
}
