package org.nutz.walnut.ext.noti.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqnbishl")
public class noti_list implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        int limit = hc.params.getInt("limit", 0);

        // 准备记录结果
        final List<WnObj> reList = new ArrayList<WnObj>(limit);

        // 进入内核态执行查找
        Wn.WC().core(null, true, null, () -> {
            // 得到要操作的用户
            String myName = sys.se.me();
            WnUsr me = sys.usrService.check(myName);

            // 得到自己在 root 组的权限
            int roleInRoot = sys.usrService.getRoleInGroup(me, "root");
            boolean I_am_member_of_root = roleInRoot == Wn.ROLE.ADMIN
                                          || roleInRoot == Wn.ROLE.MEMBER;

            // 得到消息主目录
            WnObj oNotiHome = sys.io.createIfNoExists(null, "/sys/noti", WnRace.DIR);

            // 准备条件
            WnQuery q = Wn.Q.pid(oNotiHome);

            if (hc.params.has("st"))
                q.setv("noti_st", hc.params.getInt("st"));

            // 普通用户只能查询自己的消息
            if (!I_am_member_of_root) {
                q.setv("noti_c", myName);
            }
            // 否则，可以是任何消息
            else if (hc.params.has("u")) {
                q.setv("noti_c", hc.params.get("u"));
            }

            if (limit > 0)
                q.limit(limit);

            NutMap sort = null;
            if (hc.params.has("sort")) {
                sort = Lang.map(hc.params.check("sort"));
            }
            q.sort(sort);

            // 计入结果
            sys.io.each(q, (int index, WnObj oN, int len) -> {
                reList.add(oN);
            });

            // 打印输出
            Cmds.output_objs(sys, hc.params, null, reList, false);
        });

    }

}
