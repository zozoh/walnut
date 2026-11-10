package com.site0.walnut.mvc;

/**
 * 静态文件服务：路由未命中时，对「静态资源形态」的路径（按 {@link WnMvcDispatcher}
 * 的分流规则判定）提供文件级响应。
 *
 * <p><b>RM-NUTZ-03 只定义接口与分流规则</b>；真实实现（WebContent 根 / 域静态站点 /
 * favicon 等）由 RM-NUTZ-05 提供并挂到 {@link WnMvcDispatcher}。</p>
 *
 * <p>实现必须<b>无状态、线程安全</b>。</p>
 */
@FunctionalInterface
public interface WnMvcStaticService {

    /**
     * 尝试以静态文件服务响应本次请求。
     *
     * @param context
     *            每请求上下文
     * @return <code>true</code> 表示已写出完整响应；<code>false</code> 表示文件不存在
     *         （或本服务不承接），由调度器继续走 404 回退
     */
    boolean serve(WnMvcContext context);

}
