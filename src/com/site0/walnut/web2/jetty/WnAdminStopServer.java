package com.site0.walnut.web2.jetty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.nutz.log.Log;

import com.site0.walnut.util.Wlog;

/**
 * admin 停机控制口（JETTY-04）。
 * <p>
 * <b>不是 HTTP 端口</b>，而是对齐旧 {@code org.nutz.web.WebServer} 的
 * {@code Sockets.localListenOne(adminPort, "stop", ...)}：监听一个 TCP 端口，
 * 收到一行 {@code stop}/{@code exit}/{@code bye} 就调用目标
 * {@link WnJettyServer#stop()}。
 * <p>
 * 与 {@link WnJetty9Server} 解耦：JETTY-15 换 Jetty12 时，本类不用改。
 * <p>
 * 本项目约定：{@code javax.servlet.*} / {@code org.eclipse.jetty.*} 只出现在
 * web2.jetty；本类实际只用到 JDK 的 socket API。
 */
public class WnAdminStopServer {

    private static final Log log = Wlog.getMAIN();

    private final int port;

    private final WnJettyServer target;

    private ServerSocket socket;

    private Thread thread;

    private volatile boolean stopping;

    /**
     * @param port   管理端口（admin-port；&lt;=0 表示不启用）
     * @param target 收到 stop 命令后要停止的容器
     */
    public WnAdminStopServer(int port, WnJettyServer target) {
        this.port = port;
        this.target = target;
    }

    /**
     * 绑定并启动 admin 控制口；失败时抛（由上层回滚容器）。
     */
    public synchronized void start() throws IOException {
        if (null != socket) {
            return;
        }
        if (port <= 0) {
            log.warn("WnAdminStopServer: admin-port <= 0, admin stop port disabled");
            return;
        }
        ServerSocket ss = new ServerSocket();
        ss.setReuseAddress(true);
        try {
            ss.bind(new InetSocketAddress(port));
        }
        catch (IOException e) {
            ss.close();
            throw e;
        }
        this.socket = ss;
        this.stopping = false;
        Thread t = new Thread(this::__run, "WN_ADMIN");
        t.setDaemon(true);
        this.thread = t;
        t.start();
        log.infof("WnAdminStopServer: admin stop port listening at %d (send 'stop')", port);
    }

    /**
     * 关闭控制口；幂等。可由 admin 线程或 shutdown hook 调用。
     */
    public synchronized void stop() {
        if (stopping) {
            return;
        }
        stopping = true;
        __close();
        Thread t = this.thread;
        if (null != t && t != Thread.currentThread()) {
            t.interrupt();
            try {
                t.join(1000);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // -------------------------------------------------------------
    // 内部
    // -------------------------------------------------------------

    private void __run() {
        while (!stopping) {
            ServerSocket ss = this.socket;
            if (null == ss) {
                return;
            }
            try (Socket socket = ss.accept()) {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                                                                           StandardCharsets.UTF_8));
                String line;
                while ((line = in.readLine()) != null) {
                    String cmd = line.trim();
                    if ("stop".equalsIgnoreCase(cmd)
                        || "exit".equalsIgnoreCase(cmd)
                        || "bye".equalsIgnoreCase(cmd)) {
                        log.info("WnAdminStopServer: stop command received");
                        try {
                            target.stop();
                        }
                        catch (Exception e) {
                            log.warn("WnAdminStopServer: fail to stop target server", e);
                        }
                        this.stopping = true;
                        __close();
                        return;
                    }
                }
            }
            catch (IOException e) {
                if (!stopping) {
                    log.warn("WnAdminStopServer: admin accept failed", e);
                }
            }
        }
    }

    private void __close() {
        ServerSocket ss = this.socket;
        this.socket = null;
        if (null != ss) {
            try {
                ss.close();
            }
            catch (IOException e) {
                // 关闭控制口失败无需上报
            }
        }
    }

}
