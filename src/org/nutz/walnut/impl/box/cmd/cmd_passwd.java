package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.random.StringGenerator;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

/**
 * 修改密码
 * 
 * @author wendal
 *
 */
public class cmd_passwd extends JvmExecutor {

    private static StringGenerator sg = R.sg(6);

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        String passwd;
        // .....................................................
        // 没有密码，则从标准输入读取
        if (params.vals.length == 0) {
            passwd = Strings.trim(sys.in.readAll());
        }
        // 否则第一个参数是密码
        else {
            passwd = params.val(0);
        }

        boolean printOut = false;
        if (passwd.length() < 4) {
            throw Er.create("e.cmd.passwd.tooshort");
        } else if ("wxgh_reset".equals(passwd)) {
            passwd = sg.next().toLowerCase();
            printOut = true;
        }
        // .....................................................
        // 确定用户
        String unm = params.get("u");
        WnAccount u = null;
        if (!Strings.isBlank(unm)) {
            u = sys.auth.checkAccount(unm);
        }
        // 否则就用当前会话
        else {
            u = sys.getMe();
        }
        // .....................................................
        // 修改随便的用户
        if (null != oUsr) {
            // 设置加盐后的密码
            String salt = R.UU32();
            String salt_pass = Wn.genSaltPassword(passwd, salt);
            oUsr.put("salt", salt);
            oUsr.put("passwd", salt_pass);
            sys.io.set(oUsr, "^(passwd|salt)$");
        }
        // .....................................................
        // 修改 walnut 用户
        else if (null != u) {
            // 只有 root 组的管理员才能修改其他人的密码
            if (!u.isSameId(sys.me)) {
                if (!sys.usrService.isMemberOfGroup(sys.me, "root")) {
                    throw Er.create("e.cmd.passwd.nopvg");
                }
            }

            // 执行密码修改
            sys.usrService.setPassword(u, passwd);
            if (printOut)
                sys.out.print(passwd);
        }
        // 不可能
        else {
            throw Lang.impossible();
        }
    }

}
