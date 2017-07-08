package org.nutz.walnut.ext.ftp.hdl;

import org.apache.commons.net.ftp.FTPClient;
import org.nutz.walnut.ext.ftp.FtpConfig;
import org.nutz.walnut.ext.ftp.FtpUtil;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class ftp_mv extends ftp_xxx {

    @Override
    protected boolean _invoke(WnSystem sys, JvmHdlContext hc, FTPClient client, FtpConfig conf)
            throws Throwable {
        String source = FtpUtil.ftpPath(conf, hc.params.val_check(0));
        String target = FtpUtil.ftpPath(conf, hc.params.val_check(1));
        return client.rename(source, target);
    }

}
