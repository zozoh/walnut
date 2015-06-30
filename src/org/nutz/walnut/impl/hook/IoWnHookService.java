package org.nutz.walnut.impl.hook;

import java.util.ArrayList;
import java.util.List;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.hook.WnHook;
import org.nutz.walnut.api.hook.WnHookService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

public abstract class IoWnHookService implements WnHookService {

    protected WnIo io;

    public IoWnHookService setIo(WnIo io) {
        this.io = io;
        return this;
    }

    public void on_create() {}

    protected HookReload reload(WnObj ohh) {
        HookReload hr = new HookReload();

        hr.oDir = ohh;
        if (!hr.oDir.isDIR())
            throw Er.create("e.hook.load.nodir", hr.oDir);

        List<WnObj> oHooks = io.getChildren(hr.oDir, null);
        hr.hooks = new ArrayList<WnHook>(oHooks.size());
        for (WnObj oHook : oHooks) {
            AbstractWnHook hook;
            // js
            if (oHook.isType("js")) {
                hook = new JsCommandHook();
            }
            // 模板
            else {
                hook = new TmplCommandlHook();
            }
            // 添加到结果集
            hook.init(io, oHook);
            hr.hooks.add(hook);
        }

        // 标记同步时间
        if (hr.oDir.syncTime() <= 0) {
            io.appendMeta(hr.oDir, "st:" + System.currentTimeMillis());
        }
        hr.st = hr.oDir.syncTime();

        // 返回结果集
        return hr;
    }

}
