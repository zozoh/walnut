package org.nutz.walnut.web;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Wn;

public class WnSetup implements Setup {

    private static final Log log = Logs.get();

    private WnBoxService boxes;

    @Override
    public void init(NutConfig nc) {
        Ioc ioc = nc.getIoc();

        // 获取 app 资源，并记录一下以便页面使用
        WnConfig conf = ioc.get(WnConfig.class, "conf");
        nc.setAttribute("rs", conf.getAppRs());

        // 尝试看看组装的结果
        WnIo io = ioc.get(WnIo.class, "io");
        System.out.println("io: " + io);

        // 确保有 ROOT 用户
        WnUsrService usrs = ioc.get(WnUsrService.class, "usrService");
        WnUsr root = usrs.fetch("root");
        if (root == null) {
            Wn.WC().me("root", "root");
            root = usrs.create("root", conf.get("root-init-passwd"));
            if (log.isInfoEnabled())
                log.infof("init root usr: %s", root.id());
        }

        // 获取沙箱服务
        boxes = ioc.get(WnBoxService.class, "boxService");

    }

    @Override
    public void destroy(NutConfig nc) {
        // 关闭所有运行的沙箱
        boxes.shutdown();
    }

}
