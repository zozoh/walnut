package org.nutz.walnut.ext.sshd.hdl;

import org.nutz.walnut.ext.sshd.srv.WnSshdServer;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class sshd_status implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnSshdServer sshd = hc.ioc.get(WnSshdServer.class);
        if (sshd.isRunning()) {
            sys.out.println("sshd is running at port=" + sshd.getPort());
        } else {
            sys.out.println("sshd is stoped, port=" + sshd.getPort());
        }
    }

}
