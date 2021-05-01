package org.nutz.walnut.ext.net.ftpd.hdl;

import java.io.PrintStream;

import org.nutz.walnut.ext.net.ftpd.WnFtpServer;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class ftpd_port implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnFtpServer ftpd = hc.ioc.get(WnFtpServer.class);
        try {
            ftpd.setPort(Integer.parseInt(hc.params.val(0)));
            sys.out.print("ftp port set as " + ftpd.getPort());
        }
        catch (Throwable e) {
            e.printStackTrace(new PrintStream(sys.err.getOutputStream()));
        }
    }

}
