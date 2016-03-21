package org.nutz.walnut.web;

import java.util.ArrayList;
import java.util.List;

import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.resource.Scans;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.io.WnJob;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZType;

public class WnSetup implements Setup {

    private static final Log log = Logs.get();

    private WnBoxService boxes;

    // private WnSessionService sess;

    // private WnRun wnRun;

    private List<Setup> setups;

    private Ioc ioc;

    private WnConfig conf;

    @Override
    public void init(NutConfig nc) {
        ioc = nc.getIoc();

        // 读取默认的category
        ZType.loadCategory(new PropertiesProxy("cate.properties"));

        // 获取 app 资源，并记录一下以便页面使用
        conf = ioc.get(WnConfig.class, "conf");
        nc.setAttribute("rs", conf.getAppRs());

        // 尝试看看组装的结果
        WnIo io = ioc.get(WnIo.class, "io");

        // 下面所有的操作都是 root 权限的
        Wn.WC().me("root", "root");

        // 看看初始的 mount 是否被加载
        for (WnInitMount wim : conf.getInitMount()) {
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

        // 获得session服务
        // sess = ioc.get(WnSessionService.class, "sessionService");

        // 获取沙箱服务
        boxes = ioc.get(WnBoxService.class, "boxService");

        // 获得wnRun
        //wnRun = ioc.get(WnRun.class, "wnRun");

        // etc/thumbnail
        // initThumbnail();

        // 最后加载所有的扩展 Setup
        __load_init_setups(conf);

        // 调用扩展的 Setup
        for (Setup setup : setups) {
            if (log.isInfoEnabled()) {
                log.infof("do setup: %s", setup);
            }
            setup.init(nc);
        }

        ioc.get(WnJob.class);
    }

    // zozoh: 嗯，下面的也没用了吧，过段时间删掉
    // private void initThumbnail() {
    // MimeMap mimes = ioc.get(MimeMap.class, "mimes");
    // boolean resetTB = conf.getBoolean("reset-thumbnail", false);
    // // mime中的类型
    // for (String tp : mimes.keys()) {
    // createThumbnail(tp, resetTB);
    // }
    // // 非mime类型
    // createThumbnail("folder", resetTB);
    // createThumbnail("unknow", resetTB);
    // }

    // private void createThumbnail(String tp, boolean reset) {
    // wnRun.exec("init-thumbnail",
    // "root",
    // String.format("defthumbnail -tp %s %s", tp, (reset ? "-r" : "")));
    // }

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
