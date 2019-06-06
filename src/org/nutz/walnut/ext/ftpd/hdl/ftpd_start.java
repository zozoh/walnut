package org.nutz.walnut.ext.ftpd.hdl;

import java.io.PrintStream;

import org.nutz.walnut.ext.ftpd.WnFtpServer;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class ftpd_start implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnFtpServer ftpd = hc.ioc.get(WnFtpServer.class);
        try {
            ftpd.start();
            sys.out.println("ftpd is running at port=" + ftpd.getPort());
        }
        catch (Throwable e) {
            e.printStackTrace(new PrintStream(sys.err.getOutputStream()));
        }
    }

}
