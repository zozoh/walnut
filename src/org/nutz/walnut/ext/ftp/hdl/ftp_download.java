package org.nutz.walnut.ext.ftp.hdl;

import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.ftp.FtpConfig;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class ftp_download extends ftp_xxx {

    protected boolean _invoke(WnSystem sys, JvmHdlContext hc, FTPClient client, FtpConfig conf)
            throws Throwable {
        String source = ftpPath(conf, hc.params.val_check(0));
        String target = Wn.normalizeFullPath(hc.params.val_check(1), sys);
        try (OutputStream out = sys.io.getOutputStream(sys.io.createIfNoExists(null,
                                                                               target,
                                                                               WnRace.FILE),
                                                       0)) {
            return client.retrieveFile(source, out);
        }
    }

}
