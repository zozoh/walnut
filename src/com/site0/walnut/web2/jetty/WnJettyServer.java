package com.site0.walnut.web2.jetty;

/**
 * Web 容器的最小抽象。
 * <p>
 * 上层（{@code WnLauncher2}）只面对这个接口，不 import {@code org.eclipse.jetty.*}
 * 与 {@code jakarta.servlet.*}。RM-NUTZ-14 已换 Jetty12 core，适配器只面对 core Request/Response；
 * {@code web2.jetty}（或新增的 {@code web2.jetty12}）里换实现，上层零改动。
 */
public interface WnJettyServer {

    /**
     * 启动容器（含绑定端口）。
     */
    void start() throws Exception;

    /**
     * 停止容器；可重复调用。
     */
    void stop() throws Exception;

    /**
     * 阻塞当前线程直到容器停止。
     */
    void join() throws InterruptedException;

}
