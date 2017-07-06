package org.nutz.walnut.ext.ftp.hdl;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.nutz.walnut.ext.ftp.FtpConfig;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class ftp_ls extends ftp_xxx {

    protected boolean _invoke(WnSystem sys, JvmHdlContext hc, FTPClient client, FtpConfig conf)
            throws Throwable {
        // TODO 输出ls那样的格式
        String path = ftpPath(conf, hc.params.val_check(0));
        if (!client.changeWorkingDirectory(path)) {
            sys.err.print("not such dir " + path);
            return false;
        }
        ;
        for (FTPFile dir : client.listDirectories()) {
            sys.out.printlnf("dir : %s", dir.getName());
        }
        for (FTPFile file : client.listFiles()) {
            sys.out.printlnf("file: %s %s", file.getName(), file.getSize());
        }
        return true;
    }

}
