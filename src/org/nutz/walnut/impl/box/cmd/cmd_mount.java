package org.nutz.walnut.impl.box.cmd;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.web.WnConfig;
import org.nutz.walnut.web.WnInitMount;

public class cmd_mount extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "a");
        
        if (params.vals.length == 0) {
            // TODO 检查root/op权限怎么写来着?
            if (params.is("a")) {
                WnConfig conf = ioc.get(WnConfig.class, "conf");
                for (WnInitMount wim : conf.getInitMount()) {
                    WnObj o = sys.io.createIfNoExists(null, wim.path, WnRace.DIR);
                    // 添加
                    if (Strings.isBlank(o.mount())) {
                        sys.io.setMount(o, wim.mount);
                        sys.out.printlnf("++ mount : %s > %s", wim.path, wim.mount);
                    }
                    // 修改
                    else if (!wim.mount.equals(o.mount())) {
                        sys.io.setMount(o, wim.mount);
                        sys.out.printlnf(">> mount : %s > %s", wim.path, wim.mount);
                    }
                    // 维持不变
                    else {
                        sys.out.printlnf("== mount : %s > %s", wim.path, wim.mount);
                    }
                }
            } else {
                WnQuery query = new WnQuery();
                query.exists("mnt", true).exists("data", false);
                List<WnObj> list = sys.io.query(query);
                list.sort((prev, next)-> prev.path().compareTo(next.path()));
                for (WnObj wobj : list) {
                    sys.out.printlnf("%-20s : %s", wobj.path(), wobj.mount());
                }
                return;
            }
        }

        if (params.vals.length != 2) {
            throw Er.create("e.cmd.invalidargs", args);
        }

        String mnt = params.vals[0];
        String val = params.vals[1];
        if (mnt.contains(":")) {
            throw Er.create("e.cmd.invalidargs", args);
        }

        // 目标必须是一个目录
        String ph = Wn.normalizePath(val, sys);
        WnObj oCurrent = sys.getCurrentObj();
        WnObj o = sys.io.createIfNoExists(oCurrent, ph, WnRace.DIR);

        // 不能改变当前目录的 mount，只能在父目录改变它
        if (o.isSameId(oCurrent)) {
            throw Er.create("e.cmd.mount.mountself", ph);
        }

        // 设置挂载点
        sys.io.setMount(o, mnt);
    }

}
