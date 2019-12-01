package org.nutz.walnut.ext.noti.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.noti.WnNotiHandler;
import org.nutz.walnut.ext.noti.WnNotis;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

@JvmHdlParamArgs("^(c|q|n|Q|json)$")
public class noti_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 查看消息类型
        String notiType = hc.params.val_check(0);

        // 寻找处理器
        final WnNotiHandler noti = WnNotis.checkHandler(notiType);

        // 进入内核态执行添加
        final WnContext wc = Wn.WC();
        WnObj oN = wc.core(null, true, null, new Proton<WnObj>() {
            protected WnObj exec() {
                // 得到要操作的用户
                WnAccount me = sys.getMe();

                // 得到自己在 root 组的权限
                boolean I_am_admin_of_root = sys.auth.isMemberOfGroup(me, "root");
                boolean I_am_member_of_root = I_am_admin_of_root
                                              || sys.auth.isMemberOfGroup(me, "root");

                // 修改等级设定
                int lv = hc.params.getInt("lv", 1);

                int max_lv = 1;
                // *root* 组管理员可以最大设到 1000
                if (I_am_admin_of_root) {
                    max_lv = 1000;
                }
                // *root* 组成员可最大设置到 100
                else if (I_am_member_of_root) {
                    max_lv = 100;
                }
                // TODO 以后可能增加付费用户，可最大设置到 10 （这个以后再说）
                // 其他用户怎么设置也木用，都是 1
                else {
                    max_lv = 1;
                }
                lv = Math.min(lv, max_lv);

                // 处理增加
                NutMap meta = noti.add(sys, hc.params);

                // 修改通用属性
                meta.put("tp", "wn_noti");
                meta.put("noti_c", me.getName());
                meta.put("noti_lv", lv);
                meta.put("noti_st", WnNotis.TP_WAITING);
                meta.put("noti_timeout_at", 0);
                meta.put("noti_type", notiType);
                meta.put("noti_retry", 0);
                meta.put("noti_retry_max", 3);

                // 得到消息主目录
                WnObj oNotiHome = sys.io.createIfNoExists(null, "/sys/noti", WnRace.DIR);

                // 创建消息
                WnObj oN = sys.io.create(oNotiHome, "${id}", WnRace.FILE);
                sys.io.appendMeta(oN, meta);
                return oN;
            }
        });

        // 显示输出
        if (!hc.params.is("Q")) {
            // 输出成 JSON
            if (hc.params.is("json")) {
                sys.out.println(Json.toJson(oN, hc.jfmt));
            }
            // 仅仅打印消息 ID
            else {
                sys.out.println(oN.id());
            }
        }

    }

}
