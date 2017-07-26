package org.nutz.walnut.ext.sshd.hdl;

import java.io.PrintStream;

import org.nutz.mvc.Mvcs;
import org.nutz.walnut.ext.sshd.WnSshdServer;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class sshd_port implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnSshdServer sshd = Mvcs.getIoc().get(WnSshdServer.class);
        try {
            sshd.setPort(Integer.parseInt(hc.params.val(0)));
            sys.out.print("sshd port set as " + sshd.getPort());
        }
        catch (Throwable e) {
            e.printStackTrace(new PrintStream(sys.err.getOutputStream()));
        }
    }

}
