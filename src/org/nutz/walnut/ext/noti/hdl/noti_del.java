package org.nutz.walnut.ext.noti.hdl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("Q")
public class noti_del implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        boolean quiet = hc.params.is("Q");

        // 获取指定的消息 ID
        String nid = hc.params.val_check(0);

        // 得到要操作的用户
        String myName = sys.se.me();

        // 进入内核态执行发送
        Wn.WC().core(null, true, null, () -> {
            // 得到消息主目录
            WnObj oNotiHome = sys.io.createIfNoExists(null, "/sys/noti", WnRace.DIR);

            // 找到消息
            WnObj oN = sys.io.checkById(nid);

            // 不是消息
            if (!oN.isMyParent(oNotiHome)) {
                throw Er.create("e.cmd.noti.del.not_a_msg");
            }

            // 不是我的消息，只能是 root 组的人才能删除
            if (!oN.getString("noti_c", "?").equals(myName)) {
                WnUsr me = sys.usrService.check(myName);

                // 得到自己在 root 组的权限
                int roleInRoot = sys.usrService.getRoleInGroup(me, "root");
                boolean I_am_member_of_root = roleInRoot == Wn.ROLE.ADMIN
                                              || roleInRoot == Wn.ROLE.MEMBER;

                if (!I_am_member_of_root) {
                    throw Er.create("e.cmd.noti.del.nopvg", oN.id());
                }
            }

            // 执行删除
            sys.io.delete(oN);

            // 打印结果
            if (!quiet) {
                sys.out.printlnf("rm %s(%s)[%d] > %s",
                                 oN.getString("noti_type", "?"),
                                 oN.id(),
                                 oN.getInt("noti_st", 1),
                                 oN.getString("noti_target", "?"));
            }
        });

    }

}
