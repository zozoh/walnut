package org.nutz.walnut.ext.ftpd.hdl;

import java.io.PrintStream;

import org.nutz.mvc.Mvcs;
import org.nutz.walnut.ext.ftpd.WnFtpServer;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class ftpd_start implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (sys.me.name().equals("root")) {
            WnFtpServer ftpd = Mvcs.getIoc().get(WnFtpServer.class);
            try {
                ftpd.start();
            }
            catch (Throwable e) {
                e.printStackTrace(new PrintStream(sys.err.getOutputStream()));
            }
        }
    }

}
