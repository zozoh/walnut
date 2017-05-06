package org.nutz.walnut.ext.www.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.nutz.lang.Stopwatch;
import org.nutz.log.Log;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.www.WWW;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "bishlcqno", regex = "^(warn|debug|info|trace|v)$")
public class www_rm implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到要操作的用户
        String myName = hc.params.get("u", sys.se.me());

        // 切换到内核态执行
        sys.nosecurity(new Atom() {
            public void run() {
                __do_rm(sys, hc, myName);
            }
        });
    }

    private List<WnObj> __do_rm(WnSystem sys, JvmHdlContext hc, String myName) {
        // 检查权限: 如果不是自己，那么自己必须是 root 组成员或 op 组管理员
        if (!sys.me.name().equals(myName)) {
            if (!sys.usrService.isMemberOfGroup(sys.me, "root")
                && !sys.usrService.isAdminOfGroup(sys.me, "op")) {
                throw Er.create("e.cmd.www.add.nopvg", myName);
            }
        }

        // 准备返回值
        List<WnObj> list = new ArrayList<>(hc.params.vals.length);

        // 将 www.xx.xx 作为 xx.xx 看待
        Set<String> hosts = WWW.pickHosts(hc);

        // 日志
        Log log = sys.getLog(hc.params);
        log.infof("It will remove %d host:", hosts.size());

        // 计时
        Stopwatch sw = Stopwatch.begin();

        // 循环检查指定的域名目录
        log.info("Do check:");
        WnQuery q = new WnQuery();
        q.setv("d0", "home");
        q.setv("d1", myName);
        for (String host : hosts) {
            // 根据域名映射，获取对应目录对象
            q.setv("www", host);
            WnObj oWWW = sys.io.getOne(q);
            if (null == oWWW) {
                throw Er.create("e.cmd.www.rm.noexists", host);
            }
            // 计入列表
            log.infof(" + OK : %s", host);
            list.add(oWWW);
        }

        // 将其统统删除
        log.info("Do remove:");
        for (WnObj oWWW : list) {
            log.infof(" - remove %s(%s):", oWWW.name(), oWWW.id());
            sys.execf("rm -rvf id:%s", oWWW.id());
        }

        // 停止计时
        sw.stop();
        sys.out.printf("All Done In %s", sw.toString());

        // 返回
        return list;
    }

}
