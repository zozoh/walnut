package org.nutz.walnut.web.setup;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.websocket.server.ServerContainer;

import org.nutz.ioc.Ioc;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.resource.Scans;
import org.nutz.walnut.WnVersion;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.box.WnServiceFactory;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.net.email.WnMailServer;
import org.nutz.walnut.ext.net.ftpd.WnFtpServer;
import org.nutz.walnut.ext.net.sshd.srv.WnSshdServer;
import org.nutz.walnut.ext.sys.quota.JettyMonitorHandler;
import org.nutz.walnut.ext.sys.quota.QuotaService;
import org.nutz.walnut.ext.sys.websocket.WnWebSocket;
import org.nutz.walnut.impl.box.JvmExecutorFactory;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.util.WnSysRuntime;
import org.nutz.walnut.web.WnConfig;
import org.nutz.walnut.web.WnInitMount;
import org.nutz.walnut.web.bgt.WnBgRunCronConsumer;
import org.nutz.walnut.web.bgt.WnBgRunScheduleConsumer;
import org.nutz.walnut.web.bgt.WnBgRunTaskConsumer;
import org.nutz.web.handler.JettyHandlerHook;

public class WnSetup implements Setup {

    private static final Log log = Wlog.getMAIN();

    private WnBoxService boxes;

    private List<Setup> setups;

    private Ioc ioc;

    private WnConfig conf;

    private Thread bgtTask;

    private Thread bgtSchedule;

    private Thread bgtCron;

    @Override
    public void init(NutConfig nc) {
        log.info("===============================================================");
        log.info("");
        log.info("  ╦ ╦┌─┐┬  ┌┐┌┬ ┬┌┬┐");
        log.info("  ║║║├─┤│  ││││ │ │ ");
        log.info("  ╚╩╝┴ ┴┴─┘┘└┘└─┘ ┴ ");
        log.info("  ------------------");
        log.infof("  %s", WnVersion.getName());
        log.info("");
        log.info("===============================================================");
        // 写入自身进程id
        try {
            String WL_PID_PATH = System.getenv("WL_PID_PATH");
            if (Strings.isBlank(WL_PID_PATH) && new File("/etc/ssh/sshd_config").exists())
                WL_PID_PATH = "/var/log/walnut.pid";
            String pid = Lang.JdkTool.getProcessId(null);
            log.infof("path=%s pid=%s", WL_PID_PATH, pid);
            if (!Strings.isBlank(WL_PID_PATH) && !Strings.isBlank(pid)) {
                Files.write(new File(WL_PID_PATH), pid);
            }
        }
        catch (Throwable e1) {
            log.info("something happen when writing pid file", e1);
        }
        // 获取 Ioc 容器
        ioc = nc.getIoc();

        // 获取 app 资源，并记录一下以便页面使用
        conf = ioc.get(WnConfig.class, "conf");
        nc.setAttribute("rs", conf.getAppRs());

        if (log.isInfoEnabled()) {
            List<String> keys = conf.getKeys();
            String str = "CONIFG:\n" + Strings.dup("-", 40) + "\n";
            Collections.sort(keys);
            for (String key : keys) {
                String val = conf.get(key);
                str += String.format("  @%s = %s\n", key, val);
            }
            str += Strings.dup("-", 40) + "\n";
            log.info(str);
        }

        // 初始化节点自身信息
        String nodeName = conf.get("wn-node-name");
        log.infof("Init runtime: %s", nodeName);
        WnSysRuntime rt = Wn.initRuntime(nodeName);
        log.infof(Json.toJson(Wn.getRuntime().toMap(), JsonFormat.nice()));

        // 尝试看看组装的结果
        WnIo io = Wn.Service.io(ioc);
        log.info("OK: WnIo");

        WnAuthService auth = Wn.Service.auth(ioc);
        log.info("OK: WnAuthService");

        // 获取根用户
        WnAccount root = auth.checkAccount("root");
        log.info("OK: root user");

        Wn.Service.tasks(ioc);
        log.info("OK: WnSysTaskApi");

        Wn.Service.schedules(ioc);
        log.info("OK: WnSysScheduleApi");

        Wn.Service.crons(ioc);
        log.info("OK: WnSysCronApi");

        // 下面所有的操作都是 root 权限的
        Wn.WC().setMe(root);

        // 检查一下/etc是否合法
        WnObj etc = io.fetch(null, "/etc");
        if (etc != null && etc.has("mnt")) {
            io.setBy(etc.id(), new NutMap("!mnt", ""), false);
        }
        WnObj hostsd = io.fetch(null, "/etc/hosts.d");
        if (hostsd != null && hostsd.has("mnt")) {
            io.setBy(hostsd.id(), new NutMap("!mnt", ""), false);
        }

        // 看看初始的 mount 是否被加载
        log.info("init mount:");
        for (WnInitMount wim : conf.getInitMount()) {
            try {
                WnObj o = io.createIfNoExists(null, wim.path, WnRace.DIR);
                // 添加
                if (Strings.isBlank(o.mount())) {
                    io.setMount(o, wim.mount);
                    log.infof("++ mount : %s > %s", wim.path, wim.mount);
                }
                // 修改
                else if (!wim.mount.equals(o.mount())) {
                    io.setMount(o, wim.mount);
                    log.infof(">> mount : %s > %s", wim.path, wim.mount);
                }
                // 维持不变
                else {
                    log.infof("== mount : %s > %s", wim.path, wim.mount);
                }
            }
            catch (Exception e) {
                log.warnf("!! mount : %s > %s", wim.path, wim.mount);
            }
        }

        // 看看默认的系统 hook 是否被创建了出来
        log.info("check default sys hooks ...");
        WnObj oSysHookWriteHome = io.createIfNoExists(null, "/sys/hook/write", WnRace.DIR);
        WnObj oHookCreateThumb = io.fetch(oSysHookWriteHome, "create_thumbnail");
        if (null == oHookCreateThumb) {
            log.info("  ++ /sys/hook/write/create_thumbnail");
            oHookCreateThumb = io.createIfNoExists(oSysHookWriteHome,
                                                   "create_thumbnail",
                                                   WnRace.FILE);
            io.writeText(oHookCreateThumb, "iimg id:${id} -thumb 256x256 -Q");
            NutMap hookBy = Lang.map("mime", "^image/");
            hookBy.put("ph", "!^/home/.+/(.thumbnail/gen|.publish/gen|www)");
            NutMap meta = Lang.map("hook_by", Lang.list(hookBy));
            io.appendMeta(oHookCreateThumb, meta);

        } else {
            log.info("  == /sys/hook/write/create_thumbnail");
        }

        // 获取沙箱服务
        boxes = Wn.Service.boxes(ioc);
        log.info("OK: WnBoxService");

        // 最后加载所有的扩展 Setup
        __load_init_setups(conf);

        // 调用扩展的 Setup
        for (Setup setup : setups) {
            if (log.isInfoEnabled()) {
                log.infof("do setup: %s", setup);
            }
            setup.init(nc);
        }

        ioc.get(WnSshdServer.class);
        ioc.get(WnFtpServer.class);
        ioc.get(WnMailServer.class);
        try {
            log.warn("manual setup websocket ... ");
            ServerContainer sc = (ServerContainer) nc.getServletContext()
                                                     .getAttribute(ServerContainer.class.getName());
            if (null != sc)
                sc.addEndpoint(WnWebSocket.class);
            else
                log.warn("null ServerContainer");
        }
        catch (Throwable e) {
            log.warn("主动设置websocket失败, 已经设置过了? 如果是,无视这个日志", e);
        }

        // 挂载流量统计的钩子
        JettyHandlerHook.me().setCallback(new JettyMonitorHandler(ioc.get(QuotaService.class)));
        log.debug("setup network quota hook");

        // 初始化jvm box
        ioc.get(JvmExecutorFactory.class).get("time");

        // TODO: 这个没有必要了，稍后删除，因为换成新的 WnSysCron实现了
        // 初始化Cron服务
        // if (conf.getBoolean("crontab.enable", true))
        // ioc.get(WnCronService.class);

        WnAccount guest = auth.getAccount("guest");
        if (guest == null) {
            auth.createAccount(new WnAccount("guest"));
        }

        // -----------------------------------------
        // 启动一系列线程后台处理线程
        // -----------------------------------------
        WnServiceFactory sf = ioc.get(WnServiceFactory.class, "serviceFactory");
        WnRun run = ioc.get(WnRun.class);

        //
        // 后台任务线程
        //
        Runnable bgTask = new WnBgRunTaskConsumer(sf, run);
        this.bgtTask = new Thread(bgTask, "BGT_TASK");
        this.bgtTask.start();
        log.infof("--> THREAD: %s started", this.bgtTask.getName());
        rt.props().put("BGT_TASK", true);

        // 默认开始系统后台计划任务
        boolean sysScheduleEnabled = conf.getBoolean("sys-schedule-enabled", true);
        if (sysScheduleEnabled) {
            //
            // 分钟计划任务线程
            //
            Runnable bgSchedule = new WnBgRunScheduleConsumer(sf);
            this.bgtSchedule = new Thread(bgSchedule, "BGT_SCHEDULE");
            this.bgtSchedule.start();
            log.infof("--> THREAD: %s started", this.bgtSchedule.getName());
            rt.props().put("BGT_SCHEDULE", true);

            //
            // 定期任务加载线程
            //
            int amount = conf.getInt("sys-cron-preload-amount", 5);
            Runnable bgCron = new WnBgRunCronConsumer(sf, amount);
            this.bgtCron = new Thread(bgCron, "BGT_CRON");
            this.bgtCron.start();
            log.infof("--> THREAD: %s started", this.bgtCron.getName());
            rt.props().put("BGT_CRON", true);
        }

        log.info("===============================================================");
        log.info("");
        log.info("   █     █░ ▄▄▄       ██▓     ███▄    █  █    ██ ▄▄▄█████▓");
        log.info("  ▓█░ █ ░█░▒████▄    ▓██▒     ██ ▀█   █  ██  ▓██▒▓  ██▒ ▓▒");
        log.info("  ▒█░ █ ░█ ▒██  ▀█▄  ▒██░    ▓██  ▀█ ██▒▓██  ▒██░▒ ▓██░ ▒░");
        log.info("  ░█░ █ ░█ ░██▄▄▄▄██ ▒██░    ▓██▒  ▐▌██▒▓▓█  ░██░░ ▓██▓ ░ ");
        log.info("  ░░██▒██▓  ▓█   ▓██▒░██████▒▒██░   ▓██░▒▒█████▓   ▒██▒ ░ ");
        log.info("  ░ ▓░▒ ▒   ▒▒   ▓▒█░░ ▒░▓  ░░ ▒░   ▒ ▒ ░▒▓▒ ▒ ▒   ▒ ░░   ");
        log.info("    ▒ ░ ░    ▒   ▒▒ ░░ ░ ▒  ░░ ░░   ░ ▒░░░▒░ ░ ░     ░    ");
        log.info("    ░   ░    ░   ▒     ░ ░      ░   ░ ░  ░░░ ░ ░   ░      ");
        log.info("      ░          ░  ░    ░  ░         ░    ░              ");
        log.info("");
        log.info("===============================================================");
        log.infof("                %s ready for you now", WnVersion.getName());
        log.info("===============================================================");
    }

    private void __load_init_setups(WnConfig conf) {
        setups = new ArrayList<Setup>();
        for (String str : conf.getInitSetup()) {
            if (log.isInfoEnabled()) {
                log.info("scan setup: " + str);
            }
            // 是一个类吗？
            try {
                Class<?> klass = Class.forName(str);
                Mirror<?> mi = Mirror.me(klass);
                if (log.isDebugEnabled()) {
                    log.debug("  - found class: " + klass.getName());
                }
                if (mi.isOf(Setup.class)) {
                    if (log.isDebugEnabled()) {
                        log.debug("    ... and it is a Setup");
                    }
                    Setup setup = (Setup) mi.born();
                    setups.add(setup);
                }
            }
            // 那么就是个包咯
            catch (ClassNotFoundException e) {
                List<Class<?>> klasses = Scans.me().scanPackage(str);
                if (log.isDebugEnabled()) {
                    log.debugf("  - scan package: '%s' -> %d items", str, klasses.size());
                }
                for (Class<?> klass : klasses) {
                    Mirror<?> mi = Mirror.me(klass);
                    if (mi.isOf(Setup.class)) {
                        if (log.isDebugEnabled()) {
                            log.debug("      - found Setup: " + klass.getName());
                        }
                        Setup setup = (Setup) mi.born();
                        setups.add(setup);
                    }
                }
            }

        }
    }

    @Override
    public void destroy(NutConfig nc) {
        // 调用扩展的 Setup
        for (Setup setup : setups)
            setup.destroy(nc);
        // 关闭所有运行的沙箱
        boxes.shutdown();
    }

}
