package org.nutz.walnut.impl.box.cmd;

import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;

public class userdel extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        if (!"root".equals(sys.me.name())) {
            sys.err.print("only root can do that");
            return;
        }
        String name = args[0];
        WnUsr usr = sys.usrService.check(name);
        if ("root".equals(usr.name())) {
            sys.err.print("root can't delete");
            return;
        }
        sys.exec("rm /sys/usr/" + usr.name());
        sys.exec("rm /sys/grp/" + usr.name());
        sys.exec("rm -r /home/" + usr.name());
    }

}
