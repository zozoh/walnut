package com.site0.walnut.web2.controller.httpapi;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.log.WnLog;

import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

/**
 * RM-NUTZ-09：照抄旧 {@code 旧 HTTP API 模块.__do_preload}（922-1019）。
 * 预加载命令的执行走引擎继承自 {@code WnRun} 的 {@code exec}。
 */
public class ApiPreload {

    private static final WnLog log = Wlog.getCMD();

    private final HttpApiEngine engine;

    public ApiPreload(HttpApiEngine engine) {
        this.engine = engine;
    }

    @SuppressWarnings("unchecked")
    public void doPreload(WnHttpApiContext apc) {
        Object preloadObj = apc.oApi.get("preload");
        if (null == preloadObj)
            return;

        List<NutMap> preloads = new LinkedList<>();
        if (preloadObj instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) preloadObj;
            for (Object o : coll) {
                if (o instanceof Map<?, ?>) {
                    preloads.add(NutMap.WRAP((Map<String, Object>) o));
                }
            }
        }
        // 单个
        else if (preloadObj instanceof Map<?, ?>) {
            preloads.add(NutMap.WRAP((Map<String, Object>) preloadObj));
        }

        if (preloads.isEmpty())
            return;

        // 寻找到第一个可被处理的预加载命令
        boolean isPreloaded = false;
        for (NutMap plItem : preloads) {
            Object test = plItem.get("test");
            WnMatch am = AutoMatch.parse(test, true);

            // 判断是否符合条件
            if (!am.match(apc.reqMeta)) {
                continue;
            }

            // 置否需要预先转换
            NutMap appends = plItem.getAs("appends", NutMap.class);
            if (null != appends && !appends.isEmpty()) {
                NutMap more = (NutMap) Wn.explainObj(apc.reqMeta, appends);
                if (null != more && !more.isEmpty()) {
                    apc.reqMeta.putAll(more);
                }
            }

            // 执行
            NutMap run = plItem.getAs("run", NutMap.class);
            if (null != run && !run.isEmpty()) {
                for (Map.Entry<String, Object> en : run.entrySet()) {
                    String key = en.getKey();
                    Object cmd = en.getValue();
                    if (null == cmd || "!...".equals(key))
                        continue;

                    // 分析键
                    boolean isPureText = key.startsWith("!");
                    if (isPureText) {
                        key = key.substring(1).trim();
                    }

                    // 执行命令
                    WnTmpl tmpl = WnTmpl.parse(cmd.toString());
                    String cmdText = tmpl.render(apc.reqMeta);
                    try {
                        String re = engine.exec("api-preload", apc.se, cmdText);

                        // 解析结果
                        Object reo = re;
                        if (!isPureText) {
                            reo = Json.fromJson(re);
                        }

                        // 计入结果：全部
                        if ("...".equals(key)) {
                            if (reo instanceof Map) {
                                apc.reqMeta.putAll((Map<String, Object>) reo);
                            }
                        }
                        // 计入结果：指定键
                        else {
                            apc.reqMeta.put(key, reo);
                        }
                    }
                    catch (Exception e) {
                        if (log.isWarnEnabled()) {
                            log.warn("Fail to run preload: " + key + " : " + cmd, e);
                        }
                    }
                }

                // 标记一下已经执行了预加载
                isPreloaded = true;
            }

            // 跳出
            break;
        }

        // 如果执行了 preload，则进行标志
        apc.reqMeta.put("api-preloaded", isPreloaded);
    }

}
