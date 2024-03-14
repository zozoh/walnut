package com.site0.walnut.ext.net.ftp.hdl;

import org.apache.commons.net.ftp.FTPClient;
import com.site0.walnut.ext.net.ftp.FtpConfig;
import com.site0.walnut.ext.net.ftp.FtpUtil;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

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
