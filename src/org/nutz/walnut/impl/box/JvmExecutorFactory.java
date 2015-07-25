package org.nutz.walnut.impl.box;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nutz.ioc.Ioc;
import org.nutz.lang.Each;
import org.nutz.lang.Mirror;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.resource.Scans;

public class JvmExecutorFactory {

    private static final Log log = Logs.get();

    /**
     * 要搜索的包路径
     */
    String[] scanPkgs;

    /**
     * Ioc 接口，以便执行器能用到
     */
    Ioc ioc;

    /**
     * 缓存搜索结果
     */
    private Map<String, JvmExecutor> map;

    public JvmExecutor get(String name) {
        if (null == map) {
            synchronized (this) {
                if (null == map) {
                    Map<String, JvmExecutor> map = new HashMap<String, JvmExecutor>();
                    // 搜索包
                    for (String pkg : scanPkgs) {
                        List<Class<?>> list = Scans.me().scanPackage(pkg);
                        for (Class<?> klass : list) {
                            // 跳过抽象类
                            if (Modifier.isAbstract(klass.getModifiers()))
                                continue;
                            // 跳过内部类
                            if (klass.getName().contains("$"))
                                continue;

                            // 看看是不是一个 JvmExecutor
                            Mirror<?> mi = Mirror.me(klass);
                            if (mi.isOf(JvmExecutor.class)) {
                                JvmExecutor je = (JvmExecutor) mi.born();
                                je.ioc = ioc;
                                String nm = klass.getSimpleName().substring("cmd_".length());
                                map.put(nm, je);
                                if (log.isInfoEnabled())
                                    log.infof("jvmexec: '%s' -> %s", nm, klass.getName());
                            }
                        }
                    }
                    this.map = map;
                }
            }
        }
        // 返回
        return map.get(name);
    }

    public Set<String> keys() {
        return map.keySet();
    }

    public JvmExecutor put(String nm, JvmExecutor exec) {
        return map.put(nm, exec);
    }

    public JvmExecutor remove(String nm) {
        return map.remove(nm);
    }

    public void each(Each<Entry<String, JvmExecutor>> callback) {
        for (Entry<String, JvmExecutor> en : map.entrySet()) {
            callback.invoke(0, en, map.size());
        }
    }
}
