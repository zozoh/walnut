package org.nutz.walnut.impl.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.hook.WnHook;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

public class CachedWnHookService extends IoWnHookService {

    private Map<String, HookReload> caches;//组级钩子
    private Map<String, HookReload> gcaches;//系统级钩子

    public CachedWnHookService() {
        caches = new HashMap<String, HookReload>();
        gcaches = new HashMap<String, HookReload>();
    }

    @Override
    public List<WnHook> get(final String action, WnObj o) {
        List<WnHook> list = new ArrayList<>();
        // 获取全局钩子
        list.addAll(getHooks("/sys/hook/"+action, action, o, gcaches));

        // 根据对象，得到其组的主目录
        String grp = o.group();
        String hph = "root".equals(grp) ? "/root" : "/home/" + grp;
        // 添加用户自身的钩子
        List<WnHook> userHooks = getHooks(hph + "/.hook/" + action, action, o, caches);
        if (userHooks.size() > 0) {
            for (WnHook wnHook : userHooks) {
                wnHook.setRunby(null); // 非全局钩子,不允许使用runby
                list.add(wnHook);
            }
        }
        
        return list;
    }
    
    protected List<WnHook> getHooks(String hp, String action, WnObj o, Map<String, HookReload> caches) {
        final WnContext wc = Wn.WC();
        WnObj ohh = wc.security(null, new Proton<WnObj>() {
            protected WnObj exec() {
                return wc.hooking(null, new Proton<WnObj>() {
                    protected WnObj exec() {
                        WnObj oDir = io.createIfNoExists(null, hp, WnRace.DIR);
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
