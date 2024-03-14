package com.site0.walnut.ext.net.sshd.hdl;

import java.io.PrintStream;

import com.site0.walnut.ext.net.sshd.srv.WnSshdServer;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class sshd_port implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnSshdServer sshd = hc.ioc.get(WnSshdServer.class);
        try {
            sshd.setPort(Integer.parseInt(hc.params.val(0)));
            sys.out.print("sshd port set as " + sshd.getPort());
        }
        catch (Throwable e) {
            e.printStackTrace(new PrintStream(sys.err.getOutputStream()));
        }
    }

}
