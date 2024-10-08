package com.site0.walnut.impl.hook;

import java.util.ArrayList;
import java.util.List;

import org.nutz.trans.Proton;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.hook.WnHook;
import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.esi.ElasticsearchService;
import com.site0.walnut.ext.data.esi.EsiHook;
import com.site0.walnut.impl.io.WnEvalLink;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;

public abstract class IoWnHookService implements WnHookService {

    protected WnIo io;
    
    protected ElasticsearchService esi;

    public IoWnHookService setIo(WnIo io) {
        this.io = io;
        return this;
    }

    public void on_create() {}

    protected HookInfo reload(WnObj ohh) {
        HookInfo hr = new HookInfo();

        hr.oDir = ohh;
        if (!hr.oDir.isDIR())
            throw Er.create("e.hook.load.nodir", hr.oDir);

        WnContext wc = Wn.WC();
        List<WnObj> oHooks = wc.security(null, new Proton<List<WnObj>>() {
            protected List<WnObj> exec() {
                return io.query(Wn.Q.pid(hr.oDir).asc("nm"));
            }
        });
        hr.hooks = new ArrayList<WnHook>(oHooks.size());
        WnEvalLink secu = new WnEvalLink(io);
        for (WnObj oHook : oHooks) {
            AbstractWnHook hook;
            // 隐藏文件，忽略
            if (oHook.isHidden())
                continue;
            // js
            if (oHook.isType("js")) {
                hook = new JsCommandHook();
            }
            // esi
            else if (oHook.name().equals("esi")) {
                if (!esi.isEnable())
                    continue; // 没启用esi,就没必要加了
                hook = new EsiHook(esi, oHook.parent().name());
            }
            // 模板
            else {
                hook = new TmplCommandlHook();
            }
            // 添加到结果集
            wc.security(secu, () -> hook.init(io, oHook));
            hr.hooks.add(hook);
        }

        // 标记同步时间
        if (hr.oDir.syncTime() <= 0) {
            io.appendMeta(hr.oDir, "synt:" + Wn.now());
        }
        hr.st = hr.oDir.syncTime();

        // 返回结果集
        return hr;
    }

}
