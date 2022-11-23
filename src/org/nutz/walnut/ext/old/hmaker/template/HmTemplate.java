package org.nutz.walnut.ext.old.hmaker.template;

import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.walnut.api.io.WnObj;

public class HmTemplate {

    /**
     * 服务器端渲染代码的模板（占位符模板）
     */
    public Tmpl dom;

    /**
     * 模板用到的 jquery 插件文件对象
     */
    public WnObj oJs;

    /**
     * 模板的信息文件
     */
    public HmTemplateInfo info;

    public boolean hasDom() {
        return null != dom;
    }

}
