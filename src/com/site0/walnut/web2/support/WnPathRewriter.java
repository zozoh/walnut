package com.site0.walnut.web2.support;

/**
 * 可重写路径的请求标记（JETTY-05）。
 * <p>
 * {@link com.site0.walnut.mvc.WnMvcRequest} 接口只读；Host→域 的 A 记录需要在
 * mapping 匹配前重写路径。Jetty9 请求实现本接口，`DomainRouteFilter` 通过它改写
 * {@code getPath()}；Jetty12 实现同一接口即可。
 */
public interface WnPathRewriter {

    /**
     * 重写请求路径（必须以 <code>/</code> 开头）。
     */
    void setPath(String path);

}
