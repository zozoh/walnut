package com.site0.walnut.impl.box.cmd;

import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.random.StringGenerator;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.role.WnRoleList;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

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
            passwd = params.val_check(0);
        }

        // .....................................................
        boolean printOut = false;
        if (passwd.length() < 4) {
            throw Er.create("e.cmd.passwd.tooshort");
        }
        // TODO 这个是干啥的？
        else if ("wxgh_reset".equals(passwd)) {
            passwd = sg.next().toLowerCase();
            printOut = true;
        }
        // .....................................................
        // 确定用户
        WnUser me = sys.getMe();
        WnUser u;
        String unm = params.get("u");
        // 对于非系统用（采用域账户登录的，需要做特殊处理）
        if (!me.isSysUser()) {
            // 只能自己修改自己
            if (Ws.isBlank(unm) || me.isSameName(unm)) {
                u = me;
            }
            // 否则刚烈自爆
            else {
                throw Er.create("e.cmd.passwd.dmn_user_no_pvg", me.getName());
            }
        } else if (!Strings.isBlank(unm)) {
            u = sys.auth.checkUser(unm);
        }
        // 否则就用当前会话
        else {
            u = sys.auth.checkUserById(me.getId());
        }

        // .....................................................
        // 需要验证旧密码
        if (params.has("old") && u.hasSaltedPasswd()) {
            String oldpwd = params.getString("old");
            if (!u.isMatchedRawPasswd(oldpwd)) {
                throw Er.create("e.cmd.passwd.old_invalid");
            }
        }

        // .....................................................
        // 对于非 root/op 组的操作用户，深入检查权限
        WnRoleList myRoles = sys.roles().getRoles(me);
        if (u != me && !myRoles.isMemberOfRole("root", "op")) {
            // 如果要修改的是系统用户，那么，必须是自己修改自己才成
            if (u.isSysUser()) {
                if (!u.isSame(me)) {
                    throw Er.create("e.cmd.passwd.nopvg");
                }
            }
            // 如果要修改的是普通域用户，那么必须是其主组的管理员才行
            else {
                if (!myRoles.isAdminOfRole(u.getMainGroup())) {
                    throw Er.create("e.cmd.passwd.nopvg");
                }
            }
        }
        // .....................................................
        // 保存新密码，同时这个函数也会更新 u 的 passwd+salt
        sys.auth.updateUserPassword(u, passwd);

        // .....................................................
        // 给 wxgh_reset 用的
        if (printOut)
            sys.out.print(passwd);
    }

}
