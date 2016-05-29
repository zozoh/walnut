package org.nutz.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
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

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);
        String passwd;

        // 没有密码，则从标准输入读取
        if (params.vals.length == 0) {
            passwd = Strings.trim(sys.in.readAll());
        }
        // 否则第一个参数是密码
        else {
            passwd = params.val(0);
        }

        if (passwd.length() < 4) {
            throw Er.create("e.cmd.passwd.tooshort");
        }

        // 确定用户
        WnUsr u = sys.me;
        if (params.has("u")) {
            u = sys.usrService.check(params.get("u"));
        }

        // 只有 root 组的管理员才能修改其他人的密码
        if (!u.isSameId(sys.me)) {
            int role = sys.usrService.getRoleInGroup(sys.me, "root");
            if (Wn.ROLE.ADMIN != role) {
                throw Er.create("e.cmd.passwd.nopvg");
            }
        }

        // 执行密码修改
        sys.usrService.setPassword(u, passwd);
    }

}
