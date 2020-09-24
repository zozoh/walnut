package org.nutz.walnut.impl.box;

import org.nutz.json.JsonFormat;
import org.nutz.walnut.api.io.WnObj;
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

    /**
     * 当前路径对象
     */
    private WnObj _current;

    public WnObj getCurrentObj() {
        if (null == _current) {
            _current = sys.getCurrentObj();
        }
        return _current;
    }

}
