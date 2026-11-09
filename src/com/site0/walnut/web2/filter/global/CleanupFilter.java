package com.site0.walnut.web2.filter.global;

import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.util.Wn;

/**
 * 全局收尾过滤器（tail）：对照旧 {@code web/processor/DeposeWnContext}。
 * <p>
 * 作为 tail 过滤器在 finally 收尾中执行：无论前置/控制器是否失败，都会清理线程上下文
 * {@link Wn#Ctx}，避免上下文泄漏。
 */
public class CleanupFilter implements WnMvcFilter {

    @Override
    public void doFilter(WnMvcContext ctx) {
        Wn.Ctx.clear();
    }

}
