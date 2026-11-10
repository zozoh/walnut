package com.site0.walnut.web2;

import com.site0.walnut.util.Ws;
import com.site0.walnut.log.WnLog;

import com.site0.walnut.util.Wlog;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.boot.WnDispatcherAssembler;
import com.site0.walnut.web2.boot.WnWebBoot;
import com.site0.walnut.web2.jetty.WnAdminStopServer;
import com.site0.walnut.web2.jetty.WnJetty12Server;
import com.site0.walnut.web2.jetty.WnJettyServer;
import com.site0.walnut.web2.jetty.WnMvcJetty12Handler;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * web2 启动入口（不继承 {@code org.nutz.web.WebLauncher}）。
 * <p>
 * 串联：配置 → {@link WnServiceFactory} → {@link WnWebBoot#setup()} →
 * {@link WnJettyServer} 启动 Jetty12 → shutdown hook。
 * <p>
 * 端口沿用 {@code app-port}；{@code admin-port} 由独立 {@link WnAdminStopServer}
 * 监听（TCP 停机控制口，非 HTTP）。本任务只要求编译通过，不要求真实起容器。
 */
public class WnLauncher2 {

    private static final WnLog log = Wlog.getMAIN();

    public static void main(String[] args) throws Exception {
        // 1. 读入参（配置路径，默认 web.properties）
        String confPath = "web.properties";
        if (null != args && args.length > 0 && !Ws.isBlank(args[0])) {
            confPath = args[0];
        }
        WnConfig conf = new WnConfig(confPath);

        // 2. 组装服务（非单例）
        WnServiceFactory factory = new WnServiceFactory(conf);

        // 3. 启动引导（建目录 / store / checkup / root / on_create / BGT）
        WnWebBoot boot = new WnWebBoot(factory);
        boot.setup();

        // 4. 准备容器参数
        int port = conf.getAppPort();
        if (port <= 0) {
            port = 8080;
        }
        String host = conf.get("bind-address", "0.0.0.0");
        int adminPort = conf.getAdminPort();
        log.infof("app-port=%d, admin-port=%d (admin 为 TCP 停机控制口)", port, adminPort);

        // HTTP 容器与 admin 停机控制口解耦（RM-NUTZ-15 换容器时 admin 不用改）
        WnDispatcherAssembler assembler = new WnDispatcherAssembler(factory);
        WnMvcJetty12Handler handler = assembler.buildHandler();
        WnJettyServer server = new WnJetty12Server(host, port, handler);
        WnAdminStopServer adminServer = new WnAdminStopServer(adminPort, server);

        // 5. 注册停机钩子：先销毁 boot，再停 admin 控制口与容器
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                boot.destroy();
            }
            catch (Throwable e) {
                log.warn("WnLauncher2: fail to destroy boot", e);
            }
            try {
                adminServer.stop();
            }
            catch (Throwable e) {
                log.warn("WnLauncher2: fail to stop admin server", e);
            }
            try {
                server.stop();
            }
            catch (Throwable e) {
                log.warn("WnLauncher2: fail to stop jetty", e);
            }
        }, "WN_SHUTDOWN"));

        // 6. 启动并阻塞
        server.start();
        try {
            adminServer.start();
        }
        catch (Exception e) {
            // admin 口起不来就整体失败，避免半启动
            try {
                server.stop();
            }
            catch (Throwable e2) {
                log.warn("WnLauncher2: fail to rollback jetty start", e2);
            }
            throw e;
        }
        log.infof("walnut web2 listening on %s:%d (admin stop port: %d)", host, port, adminPort);
        server.join();
        adminServer.stop();
    }

}
