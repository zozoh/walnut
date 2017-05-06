package org.nutz.walnut.ext.www.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.www.WWW;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("bishlcqno")
public class www_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到要操作的用户
        String myName = hc.params.get("u", sys.se.me());

        // 切换到内核态执行
        List<WnObj> list = sys.nosecurity(new Proton<List<WnObj>>() {
            @Override
            protected List<WnObj> exec() {
                return __do_add(sys, hc, myName);
            }
        });

        // 执行输出
        WWW.output_www_list(sys, hc, list);
    }

    private List<WnObj> __do_add(WnSystem sys, JvmHdlContext hc, String myName) {
        // 检查权限: 如果不是自己，那么自己必须是 root/op 组成员
        if (!sys.me.name().equals(myName)) {
            if (!sys.usrService.isMemberOfGroup(sys.me, "op", "root")) {
                throw Er.create("e.cmd.www.query.nopvg", myName);
            }
        }

        // 准备返回值
        List<WnObj> list = new ArrayList<>(hc.params.vals.length);

        // 将 www.xx.xx 作为 xx.xx 看待
        Set<String> hosts = WWW.pickHosts(hc);

        // 得到父目录
        WnObj oWWWHome = WWW.getWWWHome(sys, hc, myName);

        // 循环检查域名是否存在
        WnQuery q = new WnQuery();
        q.setv("d0", "home");
        q.setv("d1", myName);
        for (String host : hosts) {
            // 首先在目标目录不存在
            WnObj oWWW = sys.io.fetch(oWWWHome, host);
            if (null != oWWW) {
                throw Er.create("e.cmd.www.add.exists", host);
            }

            // 其次在整个域也不存在
            q.setv("www", host);
            oWWW = sys.io.getOne(q);
            if (null != oWWW) {
                throw Er.create("e.cmd.www.add.exists", host);
            }
        }

        // 嗯，所有的检查通过，开始逐个创建吧
        for (String host : hosts) {
            WnObj oWWW = sys.io.create(oWWWHome, host, WnRace.DIR);
            // 准备元数据
            List<String> wwwList = new ArrayList<>(2);
            wwwList.add(host);
            // 如果是 xx.xx 形式的域名，那么要自动添加 www.xx.xx 映射
            if (host.matches("^[a-zA-Z0-9_-]+[.][a-z]+$")) {
                wwwList.add("www." + host);
            }
            // 添加元数据并记录到列表
            if (wwwList.size() == 1) {
                oWWW.setv("www", wwwList.get(0));
            }
            // 否则增加为数组
            else {
                oWWW.setv("www", wwwList);
            }
            sys.io.set(oWWW, "^www$");
            list.add(oWWW);
        }

        // 返回
        return list;
    }

}
