package com.site0.walnut.impl.box;

import org.nutz.json.JsonFormat;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.ZParams;

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

    /**
     * 是否停止后续过滤器的运行
     */
    private boolean breakExec;

    public WnObj getCurrentObj() {
        if (null == _current) {
            _current = sys.getCurrentObj();
        }
        return _current;
    }

    public boolean isBreakExec() {
        return breakExec;
    }

    public void setBreakExec(boolean breakExec) {
        this.breakExec = breakExec;
    }

}
