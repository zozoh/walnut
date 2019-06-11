package org.nutz.walnut.ext.sshd.hdl;

import java.io.PrintStream;

import org.nutz.walnut.ext.sshd.srv.WnSshdServer;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class sshd_start implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnSshdServer sshd = hc.ioc.get(WnSshdServer.class);
        if (sshd.isRunning()) {
            sys.out.println("sshd is running at port=" + sshd.getPort());
        } else {
            try {
                if (sshd.getPort() > 0) {
                    sshd.start();
                    sys.out.println("sshd is running at port=" + sshd.getPort());
                } else
                    sys.err.println("pls set sshd port first");
            }
            catch (Exception e) {
                e.printStackTrace(new PrintStream(sys.err.getOutputStream()));
            }
        }
    }

}
