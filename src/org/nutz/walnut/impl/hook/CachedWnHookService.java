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

    /**
     * 用户钩子缓存，键为组名
     */
    private Map<String, Map<String, HookInfo>> userCache;

    /**
     * 系统级钩，键为动作名
     */
    private Map<String, HookInfo> globalCache;

    public CachedWnHookService() {
        userCache = new HashMap<>();
        globalCache = new HashMap<>();
    }

    @Override
    public String toString() {
        String s = "CacheHooks: G:" + globalCache.size();
        for (String key : userCache.keySet()) {
            Map<String, HookInfo> val = userCache.get(key);
            s += ", " + key + ":" + val.size();
        }
        return s;
    }

    @Override
    public List<WnHook> get(final String action, WnObj o) {
        List<WnHook> list = new ArrayList<>();
        // 根据对象，得到其组的主目录
        String grp = o.group();

        //
        // 获取用户自己的钩子
        //
        Map<String, HookInfo> cache;
        // 首先得到用户自己的缓存
        synchronized (this) {
            cache = userCache.get(grp);
            if (null == cache) {
                cache = new HashMap<>();
                userCache.put(grp, cache);
            }
        }

        // 自己钩子的主目录
        String hph = "root".equals(grp) ? "/root" : "/home/" + grp;
        // 添加用户自身的钩子
        String hookActionHomePath = hph + "/.hook/" + action;
        List<WnHook> userHooks = getHooks(hookActionHomePath, action, o, cache);
        if (userHooks.size() > 0) {
            for (WnHook wnHook : userHooks) {
                wnHook.setRunby(null); // 非全局钩子,不允许使用runby
                list.add(wnHook);
            }
        }

        //
        // 获取全局钩子
        //
        String gHookActionHome = "/sys/hook/" + action;
        List<WnHook> glist = getHooks(gHookActionHome, action, o, globalCache);
        list.addAll(glist);

        return list;
    }

    /**
     * @param hookHomePath
     *            钩子主目录
     * @param action
     *            动作，譬如 <code>write|read|create|delete</code> 等
     * @param o
     *            触发这个钩子是，操作的对象
     * @param cache
     *            缓存
     * @return 针对该动作，合适的钩子列表
     */
    protected List<WnHook> getHooks(String hookHomePath,
                                    String action,
                                    WnObj o,
                                    Map<String, HookInfo> cache) {

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
            HookInfo hi = cache.get(action);
            // 在必要的时候重新加载缓存
            if (null == hi || hi.st != oHookHome.syncTime()) {
                // 这里保证一下线程安全性
                synchronized (this) {
                    oHookHome = __refetch_hook_home(hookHomePath);
                    if (null != oHookHome) {
                        hi = cache.get(action);
                        if (null == hi || hi.st != oHookHome.syncTime()) {
                            hi = this.reload(oHookHome);
                            cache.put(action, hi);
                        }
                    }
                }
            }
            // 逐个看看钩子是否匹配
            for (WnHook hook : hi.hooks) {
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
                    io.appendMeta(oDir, "synt:" + Wn.now());
                }
                return oDir;
            }
        });
        return oHookHome;
    }
}
