package com.site0.walnut.ext.net.sshd.hdl;

import java.io.IOException;
import java.io.PrintStream;

import com.site0.walnut.ext.net.sshd.srv.WnSshdServer;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class sshd_stop implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnSshdServer sshd = hc.ioc.get(WnSshdServer.class);
        if (sshd.isRunning()) {
            try {
                sshd.stop();
                sys.out.println("sshd is stoped, port=" + sshd.getPort());
            }
            catch (IOException e) {
                e.printStackTrace(new PrintStream(sys.err.getOutputStream()));
            }
        } else {
            sys.out.println("sshd is stoped, port=" + sshd.getPort());
        }
    }

}
