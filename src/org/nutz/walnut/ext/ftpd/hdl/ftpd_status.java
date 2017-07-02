package org.nutz.walnut.ext.ftpd.hdl;

import org.nutz.mvc.Mvcs;
import org.nutz.walnut.ext.ftpd.WnFtpServer;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class ftpd_status implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        WnFtpServer ftpd = Mvcs.getIoc().get(WnFtpServer.class);
        boolean status = ftpd.isRunning();
        if (status) {
            sys.out.print("running : port = " + ftpd.getPort());
        } else {
            sys.out.print("stoped");
        }
    }

}
