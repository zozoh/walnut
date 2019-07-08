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
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;

public class CachedWnHookService extends IoWnHookService {

    private Map<String, HookReload> caches;// 组级钩子
    private Map<String, HookReload> gcaches;// 系统级钩子

    public CachedWnHookService() {
        caches = new HashMap<String, HookReload>();
        gcaches = new HashMap<String, HookReload>();
    }

    @Override
    public List<WnHook> get(final String action, WnObj o) {
        List<WnHook> list = new ArrayList<>();

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

        // 获取全局钩子
        list.addAll(getHooks("/sys/hook/" + action, action, o, gcaches));

        return list;
    }

    protected List<WnHook> getHooks(String hookHomePath,
                                    String action,
                                    WnObj o,
                                    Map<String, HookReload> caches) {

        // 得到钩子的主目录，同时确保这个目录被标记上了 st 元数据，这样子文件有更新可以立即知道
        WnObj oHookHome = __refetch_hook_home(hookHomePath);

        // 准备返回列表
        List<WnHook> list = new LinkedList<WnHook>();

        if (null == oHookHome)
            return list;

        // 对于钩子文件，不再触发钩子
        if (o.isMyAncestor(oHookHome)) {
            return list;
        }

        // 开始查找相关的钩子
        if (null != oHookHome) {
            HookReload cache = caches.get(action);
            // 在必要的时候重新加载缓存
            if (null == cache || cache.st != oHookHome.syncTime()) {
                // 这里保证一下线程安全性
                synchronized (this) {
                    oHookHome = __refetch_hook_home(hookHomePath);
                    if (null != oHookHome) {
                        cache = caches.get(action);
                        if (null == cache || cache.st != oHookHome.syncTime()) {
                            cache = this.reload(oHookHome);
                            caches.put(action, cache);
                        }
                    }
                }
            }
            // 逐个看看钩子是否匹配
            for (WnHook hook : cache.hooks) {
                if (hook.match(o))
                    list.add(hook);
            }
        }

        // 返回结果列表
        return list;
    }

    private WnObj __refetch_hook_home(String hookHomePath) {
        WnObj oHookHome = Wn.WC().core(new WnEvalLink(io), false, null, new Proton<WnObj>() {
            protected WnObj exec() {
                WnObj oDir = io.fetch(null, hookHomePath);
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
        return oHookHome;
    }
}
