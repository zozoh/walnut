package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class cmd_mode extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        ZParams params = ZParams.parse(args, "f", "^(octal|real)$");
        String input = params.val_check(0);
        boolean isForFile = params.is("f");
        boolean isReal = params.is("real");
        String uName = params.getString("u");

        // 获取用户
        WnAccount me = sys.getMe();
        if (!Ws.isBlank(uName)) {
            if (!me.isSysAccount()) {
                throw Er.create("e.cmd.mode.forbid.NoSysAccount");
            }
            if (!me.isSameName(uName)) {
                if (!sys.auth.isMemberOfGroup(me, "root", "op")) {
                    throw Er.create("e.cmd.mode.forbid.NoRight");
                }
            }
            me = sys.auth.checkAccount(uName);
        }

        // 获取目标文件
        WnObj o = null;
        int md;
        if (isForFile) {
            o = Wn.checkObj(sys, input);
            md = o.getCustomizedPrivilege(me);
            if (md < 0 && isReal) {
                md = o.mode();
            }
        }
        // 输入直接就是一个模式
        // "0777" : 强制指定八进制
        else if (params.is("octal")) {
            md = Wn.Io.modeFromOctal(input);
        }
        // "0777" : 八进制
        // 511 : 十进制
        // "rwxr-xr-x" : 全文本
        // "rwx" : 文本，相当于 "rwxrwxrwx"
        // 7 : 0-7 整数相当于 "0777"
        else {
            md = Wn.Io.modeFromStr(input);
        }

        // 八进制以及文本的表现形式
        String oct = Integer.toOctalString(md);
        String mds = Wn.Io.modeToStr(md);

        // 输出
        if (isForFile) {
            sys.out.printlnf("usr: %s", me);
            sys.out.printlnf("obj: %s", o.toString());
        }
        if (md < 0) {
            sys.out.println("NO_PVG");
        } else {
            sys.out.printlnf("mod: %s", mds);
            sys.out.printlnf("oct: %s", oct);
            sys.out.printlnf("int: %d", md);
        }
    }

}
