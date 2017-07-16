package org.nutz.walnut.ext.ftp.hdl;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
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
        WnObj wobj = sys.io.check(null, source);
        if (wobj.isDIR()) {
            sys.io.walk(wobj, new Callback<WnObj>() {
                public void invoke(WnObj obj) {
                    String rpath = target + obj.path().substring(source.length());
                    try {
                        if (obj.isFILE()) {
                            upload(obj, rpath, client, sys);
                        } else if (obj.isDIR()) {
                            client.makeDirectory(rpath);
                        }
                    }
                    catch (IOException e) {
                        sys.out.println("FAIL : " + obj.path() + " " + e.getMessage());
                    }
                }
            }, WalkMode.DEPTH_LEAF_FIRST);
            return true;
        } else {
            return upload(wobj, target, client, sys);
        }
    }

    protected boolean upload(WnObj source, String target, FTPClient client, WnSystem sys)
            throws IOException {
        try (InputStream ins = sys.io.getInputStream(source, 0)) {
            return client.storeFile(target, ins);
        }
    }
}
