package com.site0.walnut.ext.net.ftp.hdl;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import com.site0.walnut.ext.net.ftp.FtpConfig;
import com.site0.walnut.ext.net.ftp.FtpUtil;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class ftp_ls extends ftp_xxx {

    protected boolean _invoke(WnSystem sys, JvmHdlContext hc, FTPClient client, FtpConfig conf)
            throws Throwable {
        // TODO 输出ls那样的格式
        String path = FtpUtil.ftpPath(conf, hc.params.val_check(0));
        if (!client.changeWorkingDirectory(path)) {
            sys.err.print("not such dir " + path);
            return false;
        }
        for (FTPFile file : client.listFiles()) {
            sys.out.printlnf("%s : %s %s", file.isDirectory() ? "DIR " : "FILE", file.getName(), file.getSize());
        }
        return true;
    }

}
