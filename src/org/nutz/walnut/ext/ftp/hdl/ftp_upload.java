package org.nutz.walnut.ext.ftp.hdl;

import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.nutz.walnut.ext.ftp.FtpConfig;
import org.nutz.walnut.ext.ftp.FtpUtil;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class ftp_upload extends ftp_xxx {

    protected boolean _invoke(WnSystem sys, JvmHdlContext hc, FTPClient client, FtpConfig conf)
            throws Throwable {
        String source = Wn.normalizeFullPath(hc.params.val_check(0), sys);
        String target = FtpUtil.ftpPath(conf, hc.params.val_check(1));
        try (InputStream ins = sys.io.getInputStream(sys.io.check(null, source), 0)) {
            return client.storeFile(target, ins);
        }
    }

}
