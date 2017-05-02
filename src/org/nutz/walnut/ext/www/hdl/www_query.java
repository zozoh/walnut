package org.nutz.walnut.ext.www.hdl;

import java.util.List;

import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("bishlcqn")
public class www_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到要查询的用户
        String myName = hc.params.get("u", sys.se.me());

        // 切换到内核态执行
        List<WnObj> list = sys.nosecurity(new Proton<List<WnObj>>() {
            @Override
            protected List<WnObj> exec() {
                return __do_query(sys, hc, myName);
            }
        });

        // 执行输出
        hc.params.setv("l", true);
        Cmds.output_objs(sys, hc.params, null, list, false);
    }

    private List<WnObj> __do_query(WnSystem sys, JvmHdlContext hc, String myName) {
        // 检查权限: 如果不是自己，那么自己必须是 root/op 组成员
        if (!sys.me.name().equals(myName)) {
            if (!sys.usrService.isMemberOfGroup(sys.me, "op", "root")) {
                throw Er.create("e.cmd.www.query.nopvg", myName);
            }
        }

        // 准备查询条件
        WnQuery q = new WnQuery();
        q.setv("d0", "home");
        q.setv("d1", myName);
        q.setv("www", "");
        q.setv("race", WnRace.DIR);
        q.asc("nm");

        // 得到结果
        List<WnObj> list = sys.io.query(q);

        // 循环查询结果，看看在域名映射表里过期时间设置
        WnObj oDmnHome = sys.io.check(null, "/domain");
        q = Wn.Q.pid(oDmnHome);
        for (WnObj oWWW : list) {
            q.setv("dmn_grp", oWWW.get("www"));
            WnObj oDmn = sys.io.getOne(q);
            // 找到记录后，添入列表项
            if (null != oDmn) {
                oWWW.putAll(oDmn.pick("dmn_grp", "dmn_host", "dmn_expi"));
            }
        }

        // 返回
        return list;
    }

}
