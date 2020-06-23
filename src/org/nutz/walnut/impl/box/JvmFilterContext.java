package org.nutz.walnut.impl.box;

import org.nutz.json.JsonFormat;
import org.nutz.walnut.util.ZParams;

public abstract class JvmFilterContext {

    /**
     * 系统运行时
     */
    public WnSystem sys;

    /**
     * 全局（命令级）参数
     */
    public ZParams params;

    /**
     * JSON 格式化
     */
    public JsonFormat jfmt;

}
