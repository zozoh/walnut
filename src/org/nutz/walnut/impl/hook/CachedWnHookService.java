package org.nutz.walnut.impl.hook;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.hook.WnHook;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class CachedWnHookService extends IoWnHookService {

    private Map<String, HookReload> caches;

    public CachedWnHookService() {
        caches = new HashMap<String, HookReload>();
    }

    @Override
    public List<WnHook> get(final String action, WnObj o) {
        // 根据对象，得到其组的主目录
        final String grp = o.group();
        final String hph = "root".equals(grp) ? "/root" : "/home/" + grp;
        final WnContext wc = Wn.WC();
        WnObj ohh = wc.security(null, new Proton<WnObj>() {
            protected WnObj exec() {
                return wc.hooking(null, new Proton<WnObj>() {
                    protected WnObj exec() {
                        WnObj oDir = io.fetch(null, hph + "/.hook/" + action);
                        if (null == oDir)
                            return null;
                        if (!oDir.isDIR())
                            throw Er.create("e.hook.home.noDir");
                        if (oDir.syncTime() <= 0) {
                            io.appendMeta(oDir, "st:" + System.currentTimeMillis());
                        }
                        return oDir;
                    }
                });
            }
        });

        List<WnHook> list = new LinkedList<WnHook>();
        if (null != ohh) {
            HookReload cache = caches.get(action);

            // 在必要的时候重新加载缓存
            if (null == cache || cache.st != ohh.syncTime()) {
                cache = this.reload(ohh);
                caches.put(action, cache);
            }

            // 获取钩子
            for (WnHook hook : cache.hooks) {
                if (hook.match(o))
                    list.add(hook);
            }
        }

        // 返回钩子
        return list;
    }
}
