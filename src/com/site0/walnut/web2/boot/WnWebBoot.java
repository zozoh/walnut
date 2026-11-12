package com.site0.walnut.web2.boot;

import java.io.File;

import com.site0.walnut.util.Wfiles;
import com.site0.walnut.util.Ws;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.log.WnLog;

import com.site0.walnut.WnVersion;
import com.site0.walnut.api.io.WnExpiObjTable;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnSysRuntime;
import com.site0.walnut.util.WnConfig;
import com.site0.walnut.web2.bgt.WnBgCronThread;
import com.site0.walnut.web2.bgt.WnBgScheduleThread;
import com.site0.walnut.web2.bgt.WnBgTaskThread;
import com.site0.walnut.web2.bgt.WnExpiObjTableCleaner;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * web2 启动引导（对照旧 {@code WnSetup.init/destroy} 逐段重建，去掉 Nutz 相关段）。
 * <p>
 * 启动顺序（空库首启教训）：写 PID → 记录 app-rs → Wn.initRuntime →
 * <b>先建关键目录</b> → factory.initStores() → factory.checkup() → root/guest →
 * factory.initServices() → 启动 BGT 三线程。
 * <p>
 * 停机：停 BGT → factory.depose()（幂等）。
 * <p>
 * 边界：不 import {@code org.nutz.mvc.Setup/NutConfig}；不实现 HTTP 动作链与能力门
 * （整体留给 RM-NUTZ-05）。
 */
public class WnWebBoot {

    private static final WnLog log = Wlog.getMAIN();

    private final WnServiceFactory factory;

    private WnConfig conf;

    /**
     * 旧 WnSetup 里挂到 Mvcs 请求属性的 {@code rs}；web2 本任务只记录。
     */
    private String rs;

    private Thread bgtTask;

    private Thread bgtSchedule;

    private Thread bgtCron;

    private Thread expiCleaner;

    private boolean setup;

    private boolean destroyed;

    public WnWebBoot(WnServiceFactory factory) {
        if (null == factory) {
            throw new IllegalArgumentException("WnWebBoot: factory is null");
        }
        this.factory = factory;
    }

    public synchronized void setup() {
        if (setup) {
            return;
        }

        // 0. 启动横幅（与收尾横幅呼应，标记 boot 起点）
        log.info("===============================================================");
        log.infof("                %s booting ...", WnVersion.getName());
        log.info("===============================================================");

        this.conf = factory.getConfig();

        // 1. 写入自身进程 id
        __writePid();

        // 2. 页面资源属性（本任务只记录）
        this.rs = conf.getAppRs();
        log.infof("app-rs: %s", rs);

        // 3. 初始化节点自身信息
        String nodeName = conf.get("wn-node-name");
        log.infof("Init runtime: %s", nodeName);
        WnSysRuntime rt = Wn.initRuntime(nodeName);

        // 4. 检查系统关键目录（必须先建目录，再建 store）
        WnIo io = factory.getIo();
        makeWalnutKeyDirIfNoExists(io);

        // 5. 建 store / 登录 / 任务服务
        factory.initStores();

        // 5.1 能力门：主线程绑定服务集合（后续 Wn.services() 可用）
        Wn.bindServices(factory.getServiceHolder());

        // 6. 全部必备 bean 就位才继续
        factory.checkup();

        // 7. 确保 root / guest 用户
        WnLoginApi auth = factory.getLoginApi();
        String passwd = conf.get("root-init-passwd", "123456");
        WnUser root = auth.addRootUserIfNoExists(passwd);
        log.infof("OK: root user: %s", root.getId());
        WnUser guest = auth.addGuestUserIfNoExists();
        log.infof("OK: guest user: %s", guest.getId());

        // 下面所有的操作都是 root 权限的
        Wn.WC().setMe(root);

        // 8. on_create（建 /sys/tasks、/sys/cron、/sys/schedule 等目录）
        factory.initServices();

        // 9. 启动后台线程
        __startBgt(rt, auth, root);

        // 10. 收尾
        setup = true;
        log.infof("===============================================================");
        log.infof("                %s ready for you now", WnVersion.getName());
        log.info("===============================================================");
    }

    public synchronized void destroy() {
        if (destroyed) {
            return;
        }
        destroyed = true;
        // 先停后台线程，再释放连接
        __stopBgt(bgtTask, "BGT_TASK");
        __stopBgt(bgtSchedule, "BGT_SCHEDULE");
        __stopBgt(bgtCron, "BGT_CRON");
        __stopBgt(expiCleaner, "BGT_EXPI_CLEAN");
        bgtTask = null;
        bgtSchedule = null;
        bgtCron = null;
        expiCleaner = null;
        factory.depose();
        log.info("WnWebBoot: destroyed");
    }

    public String getRs() {
        return rs;
    }

    public boolean isSetup() {
        return setup;
    }

    // -------------------------------------------------------------
    // 内部步骤
    // -------------------------------------------------------------

    private void __writePid() {
        try {
            String pidPath = System.getenv("WL_PID_PATH");
            if (Ws.isBlank(pidPath) && new File("/etc/ssh/sshd_config").exists()) {
                pidPath = "/var/log/walnut.pid";
            }
            String pid = Wlang.JdkTool.getProcessId(null);
            log.infof("path=%s pid=%s", pidPath, pid);
            if (!Ws.isBlank(pidPath) && !Ws.isBlank(pid)) {
                Wfiles.write(new File(pidPath), pid);
            }
        }
        catch (Throwable e) {
            log.info("something happen when writing pid file", e);
        }
    }

    private void __startBgt(WnSysRuntime rt, WnLoginApi auth, WnUser root) {
        WnSession rootSession = auth.getSessionByUserNameAndType("root", Wn.SET_BG);
        if (null == rootSession) {
            int du = auth.getSessionDuration(true);
            rootSession = auth.createSession(root, Wn.SET_BG, du);
        }

        // 后台任务线程
        WnBgTaskThread bgtTask = new WnBgTaskThread(factory, factory.getRun(), rootSession);
        bgtTask.start();
        this.bgtTask = bgtTask;
        log.infof("--> THREAD: %s started", bgtTask.getName());
        rt.props().put("BGT_TASK", true);

        // 过期对象清理线程（RM-NUTZ-15 随 web/clean 删除后补回 web2/bgt）
        boolean expiCleanEnabled = conf.getBoolean("expi-obj-clean-enabled", true);
        if (expiCleanEnabled) {
            WnExpiObjTable expiTable = factory.fetch("safeExpiObjTable", WnExpiObjTable.class);
            long expiInterval = conf.getLong("expi-obj-clean-interval", 60000L);
            int expiLimit = conf.getInt("expi-obj-clean-limit", 100);
            long expiHold = conf.getLong("expi-obj-clean-hold", expiInterval);
            WnExpiObjTableCleaner cleaner = new WnExpiObjTableCleaner(expiTable,
                                                                      factory.getIo(),
                                                                      expiInterval,
                                                                      expiLimit,
                                                                      expiHold);
            cleaner.start();
            this.expiCleaner = cleaner;
            log.infof("--> THREAD: %s started", cleaner.getName());
            rt.props().put("BGT_EXPI_CLEAN", true);
        }

        // 默认开始系统后台计划任务
        boolean sysScheduleEnabled = conf.getBoolean("sys-schedule-enabled", true);
        if (sysScheduleEnabled) {
            // 分钟计划任务线程
            WnBgScheduleThread bgtSchedule = new WnBgScheduleThread(factory);
            bgtSchedule.start();
            this.bgtSchedule = bgtSchedule;
            log.infof("--> THREAD: %s started", bgtSchedule.getName());
            rt.props().put("BGT_SCHEDULE", true);

            // 定期任务加载线程
            int amount = conf.getInt("sys-cron-preload-amount", 5);
            WnBgCronThread bgtCron = new WnBgCronThread(factory, amount);
            bgtCron.start();
            this.bgtCron = bgtCron;
            log.infof("--> THREAD: %s started", bgtCron.getName());
            rt.props().put("BGT_CRON", true);
        }
    }

    private void __stopBgt(Thread t, String name) {
        if (null == t || !t.isAlive()) {
            return;
        }
        log.infof("interrupt %s ...", name);
        t.interrupt();
        try {
            t.join(5000);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (t.isAlive()) {
            log.warnf("%s still alive after 5s, give up waiting (daemon killer will handle)",
                      name);
        }
        else {
            log.infof("%s stopped", name);
        }
    }

    /**
     * 无论如何，都需要考虑建立下面三个目录（对照旧 WnSetup.makeWalnutKeyDirIfNoExists）：
     * <ul>
     * <li><code>/sys/usr:750:root:root</code> 存放用户信息
     * <li><code>/sys/role:750:root:root</code> 存放用户角色分组
     * <li><code>/var/session:750:root:root</code> 存放会话信息
     * </ul>
     */
    private void makeWalnutKeyDirIfNoExists(WnIo io) {
        Wn.WC().nosecurity(io, () -> {
            this.__checkKeyDir(io, "/sys", 493);
            this.__checkKeyDir(io, "/sys/usr", 488);
            this.__checkKeyDir(io, "/sys/role", 488);
            this.__checkKeyDir(io, "/var", 493);
            this.__checkKeyDir(io, "/var/session", 488);
        });
    }

    private void __checkKeyDir(WnIo io, String path, int mode) {
        WnObj oDir = io.createIfNoExists(null, path, WnRace.DIR);
        NutMap delta = new NutMap();
        if (oDir.mode() != mode) {
            delta.put("md", mode);
        }
        if (!oDir.creator().equals("root")) {
            delta.put("c", "root");
        }
        if (!oDir.mender().equals("root")) {
            delta.put("m", "root");
        }
        if (!oDir.group().equals("root")) {
            delta.put("g", "root");
        }
        if (!delta.isEmpty()) {
            io.appendMeta(oDir, delta);
        }
    }

}
