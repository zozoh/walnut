package org.nutz.walnut.impl.box.cmd;

import org.nutz.json.Json;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_session extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) {
        ZParams params = ZParams.parse(args, "^v|create$");
        if (params.is("create")) {
            // 想创建session? 先检查权限
            WnUsrService us = sys.usrService;
            if (us.isInGroup(sys.me, "root") || us.isInGroup(sys.me, "opt")) {
                WnUsr usr = us.check(params.val_check(0));
                WnSession sess = sys.sessionService.create(usr);
                sys.out.println(Json.toJson(sess));
            } else {
                sys.out.println("{msg:\"not allow\"}");
            }
        } else {
            sys.out.println(Json.toJson(sys.se));
        }
        
    }

}
