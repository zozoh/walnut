package com.site0.walnut.web2.filter.global;

import org.nutz.log.Log;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * 全局收尾过滤器（tail）：对照旧 {@code web/processor/DeposeWnContext}。
 * <p>
 * 作为 tail 过滤器在 finally 收尾中执行：先恢复旧 {@code DeposeWnContext} 的
 * ✔️OK 访问日志（含耗时 du），然后无论前置/控制器是否失败，都会清理线程上下文
 * {@link Wn#Ctx}，避免上下文泄漏。
 * <p>
 * 偏差：旧链中失败请求走 {@code WnFailProcessor} 打 🚫KO，✔️OK 只出现在成功路径；
 * 本过滤器在 finally 中无条件执行，因此仅在 {@code status < 400} 时打印 ✔️OK
 * （会话拒绝的 302 仍会打印 ✔️OK302，为已知偏差）。
 */
public class CleanupFilter implements WnMvcFilter {

    private static final Log log = Wlog.getAC();

    @Override
    public void doFilter(WnMvcContext ctx) {
        // 显示调试信息（对照旧 DeposeWnContext）
        if (log.isInfoEnabled()) {
            long ts = Wn.WC()._timestamp;
            long du = ts > 0 ? Wn.now() - ts : ts;
            String ph = ctx.request().getPath();
            int status = ctx.response().status();
            // 这种 URL 暂时先不打印，因为负载均衡会狂请求 ...
            if (status < 400 && !"/".equals(ph)) {
                String qs = ctx.request().getQueryString();
                if (!Ws.isBlank(qs)) {
                    qs = "?" + qs;
                } else {
                    qs = "";
                }
                // 被偷偷标志了关闭日志
                if (qs.indexOf("log_off=true") >= 0) {
                    // 不打印（旧代码此处注释掉了 Wn.WC().setLogOff()，仅跳过日志）
                } else {
                    log.infof("✔️OK%d:%dms:%s%s", status, du, ph, qs);
                }
            }
        }

        // 执行清除
        Wn.Ctx.clear();
    }

}
