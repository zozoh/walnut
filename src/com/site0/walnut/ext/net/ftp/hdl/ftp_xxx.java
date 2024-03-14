package com.site0.walnut.ext.net.ftp.hdl;

import org.apache.commons.net.ftp.FTPClient;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.ftp.FtpConfig;
import com.site0.walnut.ext.net.ftp.FtpUtil;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public abstract class ftp_xxx implements JvmHdl {

    private static final Log log = Wlog.getCMD();
    
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        FTPClient client = null;
        try {
            FtpConfig conf = FtpUtil.conf(hc.getAs("ftpconf_obj", WnObj.class), sys.io);
            client = FtpUtil.client(conf);
            boolean re = _invoke(sys, hc, client, conf);
            if (!re) {
                sys.err.println("fail");
            }
        }
        catch (Throwable e) {
            sys.err.println(e.getMessage());
            log.debug("FTPClient wrorg?", e);
        }
        finally {
            if (client != null) {
                try {
                    client.disconnect();
                }
                catch (Throwable e) {
                    log.info("close FTPClient fail", e);
                }
            }
        }
    }

    protected abstract boolean _invoke(WnSystem sys,
                                       JvmHdlContext hc,
                                       FTPClient client,
                                       FtpConfig conf)
            throws Throwable;
}
