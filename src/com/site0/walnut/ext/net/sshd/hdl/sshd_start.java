package com.site0.walnut.ext.net.sshd.hdl;

import java.io.PrintStream;

import com.site0.walnut.ext.net.sshd.srv.WnSshdServer;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

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
