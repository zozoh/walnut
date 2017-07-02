package org.nutz.walnut.ext.ftpd.hdl;

import java.io.PrintStream;

import org.nutz.mvc.Mvcs;
import org.nutz.walnut.ext.ftpd.WnFtpServer;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class ftpd_port implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        if (sys.me.name().equals("root")) {
            WnFtpServer ftpd = Mvcs.getIoc().get(WnFtpServer.class);
            try {
                ftpd.setPort(Integer.parseInt(hc.params.val(0)));
                sys.out.print("ftp port set as " + ftpd.getPort());
            }
            catch (Throwable e) {
                e.printStackTrace(new PrintStream(sys.err.getOutputStream()));
            }
        }
    }

}
