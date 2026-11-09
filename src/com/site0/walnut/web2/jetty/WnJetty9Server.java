package com.site0.walnut.web2.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 * Jetty9 内嵌容器实现（JETTY-04 只挂占位 Handler）。
 * <p>
 * 只负责 HTTP 容器（{@code appPort}）。管理端口的停机控制口是独立组件
 * {@link WnAdminStopServer}，由上层 {@code WnLauncher2} 同时启停；这样 JETTY-15
 * 换 Jetty12 时，admin 控制通道零改动。
 * <p>
 * 占位 Handler 返回 {@code 503 not-ready}：真正的 HTTP 适配层/动作链/静态回退由
 * JETTY-05 在 {@code web2.jetty} 内实现并替换本 Handler。
 * <p>
 * 本项目约定：{@code org.eclipse.jetty.*} 与 {@code javax.servlet.*} 的 import
 * 只允许出现在本包。
 */
public class WnJetty9Server implements WnJettyServer {

    private final Server server;

    private final Handler handler;

    /**
     * 占位 Handler 构造器（JETTY-04 兼容）：无业务响应。
     */
    public WnJetty9Server(String host, int appPort) {
        this(host, appPort, new WnNotReadyHandler());
    }

    /**
     * @param host    绑定地址（空则 0.0.0.0）
     * @param appPort HTTP 监听端口（app-port）
     * @param handler 业务 Handler（JETTY-05 传 {@code WnMvcJettyHandler}）
     */
    public WnJetty9Server(String host, int appPort, Handler handler) {
        QueuedThreadPool pool = new QueuedThreadPool(50);
        pool.setName("wn-jetty");

        Server server = new Server(pool);

        // X-Powered-By / Server 版本号等价旧 Mvcs.DISABLE_X_POWERED_BY 的关闭行为
        HttpConfiguration httpConf = new HttpConfiguration();
        httpConf.setSendServerVersion(false);
        httpConf.setSendXPoweredBy(false);

        ServerConnector connector = new ServerConnector(server,
                                                        new HttpConnectionFactory(httpConf));
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

    /**
     * JETTY-04 的占位 Handler：容器能起，但不提供业务响应。
     */
    public static class WnNotReadyHandler extends AbstractHandler {

        @Override
        public void handle(String target,
                           Request baseRequest,
                           HttpServletRequest request,
                           HttpServletResponse response)
                throws IOException, ServletException {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("text/plain;charset=utf-8");
            response.getWriter().write("walnut web2: not ready (HTTP adapter lands in JETTY-05)\n");
            baseRequest.setHandled(true);
        }
    }

}
