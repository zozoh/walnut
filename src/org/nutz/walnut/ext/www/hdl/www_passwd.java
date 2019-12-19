package org.nutz.walnut.ext.www.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuths;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class www_passwd implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
        String site = hc.params.val_check(0);
        String passwd = hc.params.val_check(1);
        if (passwd.length() < 4) {
            throw Er.create("e.cmd.passwd.tooshort");
        }
        String ticket = hc.params.get("ticket");
        String unm = hc.params.get("u");
        // -------------------------------
        // 准备服务类
        WnObj oWWW = Wn.checkObj(sys, site);
        WnWebService webs = new WnWebService(sys, oWWW);
        // -------------------------------
        // 确定用户
        WnAccount me = sys.getMe();
        WnAccount u;
        // -u 模式，则当前操作会话必须为站点管理员或者 root/op组成员
        if (!Strings.isBlank(unm)) {
            // 检查权限
            if (!sys.auth.isAdminOfGroup(me, oWWW.group())) {
                if (!sys.auth.isMemberOfGroup(me, "root", "op")) {
                    throw Er.create("e.cmd.www_passwd.nopvg");
                }
            }
            // 通过
            u = webs.getAuthApi().checkAccount(unm);
        }
        // 否则就用当前会话
        else if (!Strings.isBlank(ticket)) {
            u = webs.getAuthApi().checkSession(ticket).getMe();
        }
        // unm/ticket 必须得有一个啊
        else {
            throw Er.create("e.cmd.www_passwd.LackTarget");
        }

        // -------------------------------
        // 设置并保存新密码
        u.setRawPasswd(passwd);
        webs.getAuthApi().saveAccount(u, WnAuths.ABMM.PASSWD);

    }

}
