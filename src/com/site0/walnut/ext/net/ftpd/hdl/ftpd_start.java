package com.site0.walnut.ext.net.ftpd.hdl;

import java.io.PrintStream;

import com.site0.walnut.ext.net.ftpd.WnFtpServer;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

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
