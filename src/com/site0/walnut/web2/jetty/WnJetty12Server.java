package com.site0.walnut.web2.jetty;

import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * Jetty12（core，无 servlet）内嵌容器实现（RM-NUTZ-14）。
 * <p>
 * 只负责 HTTP 容器（{@code appPort}）。管理端口的停机控制口是独立组件
 * {@link WnAdminStopServer}，由上层 {@code WnLauncher2} 同时启停。
 * <p>
 * HTTP/2 依赖已在 pom（{@code jetty-http2-server}），本实现先走 HTTP/1.1；
 * 如需 h2c 可在 {@code ServerConnector} 追加 {@code HTTP2CServerConnectionFactory}。
 * <p>
 * 本项目约定：{@code org.eclipse.jetty.*} 的 import 只允许出现在本包。
 */
public class WnJetty12Server implements WnJettyServer {

    private final Server server;

    private final Handler handler;

    /**
     * @param host    绑定地址（空则 0.0.0.0）
     * @param appPort HTTP 监听端口（app-port）
     * @param handler 业务 Handler（{@code WnMvcJetty12Handler}）
     */
    public WnJetty12Server(String host, int appPort, Handler handler) {
        QueuedThreadPool pool = new QueuedThreadPool(50);
        pool.setName("wn-jetty");
        Server server = new Server(pool);

        // X-Powered-By / Server 版本号等价旧 Mvcs.DISABLE_X_POWERED_BY 的关闭行为
        HttpConfiguration httpConf = new HttpConfiguration();
        httpConf.setSendServerVersion(false);
        httpConf.setSendXPoweredBy(false);

        // HTTP/1.1 + h2c（明文 HTTP/2）：浏览器对 http:// 明文连接只会用 HTTP/1.1，
        // h2c 面向 curl --http2-prior-knowledge / gRPC / nginx h2c upstream 等客户端
        ServerConnector connector = new ServerConnector(server,
                                                        new HttpConnectionFactory(httpConf),
                                                        new HTTP2CServerConnectionFactory(httpConf));
        connector.setHost(null == host || host.isEmpty() ? "0.0.0.0" : host);
        connector.setPort(appPort);
        server.setConnectors(new Connector[]{connector});

        server.setHandler(handler);
        server.setStopAtShutdown(false);

        this.server = server;
        this.handler = handler;
    }

    /**
     * 当前 Handler（便于装配期/测试读取）。
     */
    public Handler handler() {
        return handler;
    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void stop() throws Exception {
        if (null != server) {
            server.stop();
        }
    }

    @Override
    public void join() throws InterruptedException {
        server.join();
    }

}
