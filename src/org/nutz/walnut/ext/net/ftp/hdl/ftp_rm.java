package org.nutz.walnut.ext.net.ftp.hdl;

import org.apache.commons.net.ftp.FTPClient;
import org.nutz.walnut.ext.net.ftp.FtpConfig;
import org.nutz.walnut.ext.net.ftp.FtpUtil;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("r")
public class ftp_rm extends ftp_xxx {

    protected boolean _invoke(WnSystem sys, JvmHdlContext hc, FTPClient client, FtpConfig conf)
            throws Throwable {
        String path = FtpUtil.ftpPath(conf, hc.params.val_check(0));
        if (hc.params.is("r")) {
            return client.removeDirectory(path);
        } else {
            return client.deleteFile(path);
        }
    }

}
