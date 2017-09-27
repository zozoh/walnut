package org.nutz.walnut.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.websocket.server.ServerContainer;

import org.apache.commons.mail.HtmlEmail;
import org.nutz.filepool.UU32FilePool;
import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.resource.Scans;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.email.WnMailServer;
import org.nutz.walnut.ext.ftpd.WnFtpServer;
import org.nutz.walnut.ext.sshd.srv.WnSshdServer;
import org.nutz.walnut.ext.websocket.WnWebSocket;
import org.nutz.walnut.impl.io.bucket.MemoryBucket;
import org.nutz.walnut.job.WnJob;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZType;

public class WnSetup implements Setup {

    private static final Log log = Logs.get();

    private WnBoxService boxes;

    private List<Setup> setups;

    private Ioc ioc;

    private WnConfig conf;

    @Override
    public void init(NutConfig nc) {
        // 获取 Ioc 容器
        ioc = nc.getIoc();

        // 读取默认的category
        ZType.loadCategory(new PropertiesProxy("cate.properties"));

        // 获取 app 资源，并记录一下以便页面使用
        conf = ioc.get(WnConfig.class, "conf");
        nc.setAttribute("rs", conf.getAppRs());

        // 设置一下MemoryBucket的临时文件池
        MemoryBucket.pool = new UU32FilePool(conf.get("memory-bucket-home",
                                                      System.getProperty("java.io.tmpdir")));

        // 尝试看看组装的结果
        WnIo io = ioc.get(WnIo.class, "io");

        // 下面所有的操作都是 root 权限的
        Wn.WC().me("root", "root");

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

        // 获取沙箱服务
        boxes = ioc.get(WnBoxService.class, "boxService");

        // 最后加载所有的扩展 Setup
        __load_init_setups(conf);

        // 调用扩展的 Setup
        for (Setup setup : setups) {
            if (log.isInfoEnabled()) {
                log.infof("do setup: %s", setup);
            }
            setup.init(nc);
        }

        if (conf.getBoolean("service-wnjob", true)) {
            ioc.get(WnJob.class);
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

        // 发送邮件通知，服务器已启动
        if (conf.getBoolean("startup-noti", true)) {
            Lang.runInAnThread(new Runnable() {
                @Override
                public void run() {
                    List<String> receivers = conf.getList("noti-receivers", ",");
                    for (String to : receivers) {
                        log.infof("send startup-noti email to %s", to);
                        try {
                            HtmlEmail email = new HtmlEmail();
                            email.setHostName(conf.get("noti-host"));
                            email.setSmtpPort(conf.getInt("noti-port"));
                            email.setAuthentication(conf.get("noti-account"),
                                                    conf.get("noti-password"));
                            email.setSSLOnConnect(conf.getBoolean("noti-ssl"));
                            email.setCharset(Encoding.UTF8);
                            email.setSubject("WalnutServer is started");
                            email.setHtmlMsg("WalnutServer is started on " + Times.sDT(new Date()));
                            email.setFrom(conf.get("noti-account"), "NutzTeam");
                            email.addTo(to);
                            email.buildMimeMessage();
                            email.sendMimeMessage();
                        }
                        catch (Throwable e) {
                            log.info("send email fail", e);
                        }
                        Lang.sleep(1000);
                    }
                }
            });
        }
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
