package org.nutz.walnut.ext.sshd.hdl;

import org.nutz.mvc.Mvcs;
import org.nutz.walnut.ext.sshd.WnSshdServer;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class sshd_status implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnSshdServer sshd = Mvcs.getIoc().get(WnSshdServer.class);
        if (sshd.isRunning()) {
            sys.out.println("sshd is running at port=" + sshd.getPort());
        } else {
            sys.out.println("sshd is stoped, port=" + sshd.getPort());
        }
    }

}
