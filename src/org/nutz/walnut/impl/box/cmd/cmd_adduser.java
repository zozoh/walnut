package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrInfo;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_adduser extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, null);

        String str = params.val_check(0);
        String passwd = params.get("p", "123456");
        
        if (sys.usrService.fetch(str) != null) {
            return;
        }

        WnUsrInfo info = new WnUsrInfo(str);

        // 创建用户
        WnUsr u = sys.usrService.create(info);

        // 修改密码
        sys.usrService.setPassword(u, passwd);

    }

}
