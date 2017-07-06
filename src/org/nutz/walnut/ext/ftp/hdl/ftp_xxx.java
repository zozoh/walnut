package org.nutz.walnut.ext.ftp.hdl;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.ftp.FtpConfig;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public abstract class ftp_xxx implements JvmHdl {

    private static final Log log = Logs.get();

    protected FtpConfig conf(WnObj wobj, WnIo io) {
        return io.readJson(wobj, FtpConfig.class);
    }

    protected FTPClient client(FtpConfig conf) throws IOException {
        FTPClient client = new FTPClient();
        client.connect(conf.getHost(), conf.getPort());
        if (!Strings.isBlank(conf.getUsername())) {
            if (!client.login(conf.getUsername(), conf.getToken())) {
                client.disconnect();
                return null;
            }
            ;
        }
        return client;
    }

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        FTPClient client = null;
        try {
            FtpConfig conf = conf(hc.getAs("ftponf_obj", WnObj.class), sys.io);
            client = client(conf);
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

    protected String ftpPath(FtpConfig conf, String path) {
        if (!path.startsWith("/"))
            path = "/" + path;
        if (!Strings.isBlank(conf.getPathPrefix())) {
            path = conf.getPathPrefix() + path;
        }
        return path;
    }

    protected abstract boolean _invoke(WnSystem sys,
                                       JvmHdlContext hc,
                                       FTPClient client,
                                       FtpConfig conf)
            throws Throwable;
}
