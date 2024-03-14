package com.site0.walnut.ext.net.ftp.hdl;

import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.net.ftp.FtpConfig;
import com.site0.walnut.ext.net.ftp.FtpUtil;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class ftp_download extends ftp_xxx {

    protected boolean _invoke(WnSystem sys, JvmHdlContext hc, FTPClient client, FtpConfig conf)
            throws Throwable {
        String source = FtpUtil.ftpPath(conf, hc.params.val_check(0));
        String target = Wn.normalizeFullPath(hc.params.val_check(1), sys);
        try (OutputStream out = sys.io.getOutputStream(sys.io.createIfNoExists(null,
                                                                               target,
                                                                               WnRace.FILE),
                                                       0)) {
            return client.retrieveFile(source, out);
        }
    }

}
