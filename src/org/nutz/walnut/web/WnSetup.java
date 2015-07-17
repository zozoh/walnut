package org.nutz.walnut.web;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.nutz.ioc.Ioc;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Mirror;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.resource.Scans;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.util.ZType;

public class WnSetup implements Setup {

    private static final Log log = Logs.get();

    private WnBoxService boxes;

    private WnSessionService sess;

    private WnRun wnRun;

    private List<Setup> setups;

    @Override
    public void init(NutConfig nc) {
        Ioc ioc = nc.getIoc();

        // 初始化自定义的 IocLoader
        // WnIocLoader wnLoader = ioc.get(WnIocLoader.class, "loader");
        //
        // log.infof("loader : %s : %s",
        // wnLoader.getClass(),
        // Lang.concat("\n - ", wnLoader.getName()));

        // 读取默认的category
        ZType.loadCategory(new PropertiesProxy("cate.properties"));

        // 获取 app 资源，并记录一下以便页面使用
        WnConfig conf = ioc.get(WnConfig.class, "conf");
        nc.setAttribute("rs", conf.getAppRs());

        // 尝试看看组装的结果
        WnIo io = ioc.get(WnIo.class, "io");

        // WnRun
        wnRun = ioc.get(WnRun.class, "wnRun");

        // 下面所有的操作都是 root 权限的
        Wn.WC().me("root", "root");

        // 确保有 ROOT 用户
        WnUsrService usrs = ioc.get(WnUsrService.class, "usrService");
        WnUsr root = usrs.fetch("root");
        if (root == null) {
            root = usrs.create("root", conf.get("root-init-passwd"));
            log.infof("init root usr: %s", root.id());
        }

        // 获得session服务
        sess = ioc.get(WnSessionService.class, "sessionService");

        // 获取沙箱服务
        boxes = ioc.get(WnBoxService.class, "boxService");

        // 获得用户与se
        final WnUsr u = usrs.check("root");
        WnSession se = Wn.WC().su(u, new Proton<WnSession>() {
            protected WnSession exec() {
                return sess.create(u);
            }
        });

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

        // 最后加载所有的扩展 Setup
        __load_init_setups(conf);

        // etc/thumbnail
        WnObj thumbnailDir = io.createIfNoExists(null, "/etc/thumbnail/", WnRace.DIR);
        MimeMap mimes = ioc.get(MimeMap.class, "mimes");
        boolean resetThumbnail = conf.getBoolean("reset-thumbnail", false);
        // mime中的类型
        for (String tp : mimes.keys()) {
            boolean existThumb = io.exists(thumbnailDir, tp);
            if (existThumb) {
                if (resetThumbnail) {
                    io.delete(io.fetch(thumbnailDir, tp));
                } else {
                    continue;
                }
            }
            createDefaultThumbnail(io, se, thumbnailDir, tp);
        }
        // 未知类型
        createDefaultThumbnail(io, se, thumbnailDir, "unknow");

        // 调用扩展的 Setup
        for (Setup setup : setups)
            setup.init(nc);

    }

    private void createDefaultThumbnail(WnIo io, WnSession se, WnObj pdir, String tp) {
        WnObj tpDir = io.createIfNoExists(pdir, tp, WnRace.DIR);
        try {
            // copy对应的图片过去
            URL tpurl = this.getClass().getResource("/thumbnail/" + tp + ".png");
            String tppath = tpurl.getPath();
            if (!Strings.isBlank(tppath)) {
                File tpf = new File(tppath);
                WnObj s256 = io.createIfNoExists(tpDir,
                                                 Wn.thumbnail.size_256 + ".png",
                                                 WnRace.FILE);
                io.writeAndClose(s256, Streams.fileIn(tpf));
                createThunbnail(se, tpDir, s256, Wn.thumbnail.size_64);
                createThunbnail(se, tpDir, s256, Wn.thumbnail.size_24);
                createThunbnail(se, tpDir, s256, Wn.thumbnail.size_16);
            }
        }
        catch (Exception e) {}
    }

    private void createThunbnail(WnSession se, WnObj targetDir, WnObj src, String size) {
        wnRun.exec("default-thumbnail",
                   se,
                   null,
                   String.format("chimg %s -z -s %s -o %s",
                                 src.path(),
                                 size,
                                 targetDir.path() + "/" + size + "." + src.type()));
    }

    private void __load_init_setups(WnConfig conf) {
        setups = new ArrayList<Setup>();
        for (String str : conf.getInitSetup()) {
            // 是一个类吗？
            try {
                Class<?> klass = Class.forName(str);
                Mirror<?> mi = Mirror.me(klass);
                if (mi.isOf(Setup.class)) {
                    Setup setup = (Setup) mi.born();
                    setups.add(setup);
                }
            }
            // 那么就是个包咯
            catch (ClassNotFoundException e) {
                List<Class<?>> klasses = Scans.me().scanPackage(str);
                for (Class<?> klass : klasses) {
                    Mirror<?> mi = Mirror.me(klass);
                    if (mi.isOf(Setup.class)) {
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
