package org.nutz.walnut.impl.box.cmd;

import java.util.List;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.mapping.MountInfo;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.web.WnConfig;
import org.nutz.walnut.web.WnInitMount;

public class cmd_mount extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "cqnbishp", "^(init)$");
        WnAccount me = sys.getMe();
        boolean isSysAdmin = sys.auth.isMemberOfGroup(me, "root");
        // 初始化映射的再次检查
        if (params.is("init")) {
            if (!isSysAdmin) {
                throw Er.create("e.cmd.mount.PermissionDenied");
            }
            __sync_init_mount(sys);
            return;
        }

        // 列出所有的映射记录
        if (params.vals.length == 0) {
            if (!isSysAdmin) {
                throw Er.create("e.cmd.mount.PermissionDenied");
            }
            __list_all_mounts(sys, params);
            return;
        }

        // 必须有两个参数： 【映射】 : 【路径】
        String mnt = params.val_check(0);
        String ph = params.val_check(1);

        // 目标必须是一个目录
        WnObj o = Wn.checkObj(sys, ph);

        // 必须为对应域的管理员，才能执行
        if (!sys.auth.isAdminOfGroup(me, "root", o.group())) {
            throw Er.create("e.cmd.mount.NotAdmin");
        }

        // 不能改变当前目录的 mount，只能在父目录改变它
        WnObj oCurrent = sys.getCurrentObj();
        if (o.isSameId(oCurrent)) {
            throw Er.create("e.cmd.mount.mountself", ph);
        }

        // 解析映射
        MountInfo mi = new MountInfo(mnt);

        // 对于本地文件映射的索引管理器，检查一下白名单
        if (mi.isIndexerType("^filew?$")) {
            // 只有系统管理员才能映射本地文件
            if (!isSysAdmin) {
                throw Er.create("e.cmd.mount.PermissionDenied");
            }
            if (!__is_in_local_file_white_list(sys, mi.getIndexerArg())) {
                throw Er.create("e.cmd.mount.forbid", mi.getIndexer().toString());
            }
        }

        // 对于本地文件映射的桶管理器，检查一下白名单
        if (mi.isBMType("^filew?$")) {
            // 只有系统管理员才能映射本地文件
            if (!isSysAdmin) {
                throw Er.create("e.cmd.mount.PermissionDenied");
            }
            if (!__is_in_local_file_white_list(sys, mi.getBMArg())) {
                throw Er.create("e.cmd.mount.forbid", mi.getBM().toString());
            }
        }

        // 设置挂载点
        sys.io.setMount(o, mnt);
    }

    private boolean __is_in_local_file_white_list(WnSystem sys, String fph) {
        PropertiesProxy conf = ioc.get(PropertiesProxy.class, "conf");
        List<String> allows = conf.getList("mnt-file-allow");
        if (allows != null && allows.size() > 0) {
            for (String allow : allows) {
                if (fph.startsWith(allow)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void __list_all_mounts(WnSystem sys, ZParams params) {
        WnPager wp = new WnPager(params);
        WnQuery q = new WnQuery();
        q.setv("mnt", "");
        wp.setupQuery(sys.io, q);
        List<WnObj> list = sys.io.query(q);
        if (params.is("p")) {
            for (WnObj o : list) {
                o.path();
            }
        }
        Cmds.output_beans(sys, params, wp, list);
    }

    private void __sync_init_mount(WnSystem sys) {
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
    }

}
