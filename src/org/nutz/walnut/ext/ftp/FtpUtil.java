package org.nutz.walnut.ext.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

public class FtpUtil {
    
    private static final Log log = Logs.get();

    public static FtpConfig conf(WnObj wobj, WnIo io) {
        return io.readJson(wobj, FtpConfig.class);
    }

    public static FTPClient client(FtpConfig conf) throws IOException {
        FTPClient client = new FTPClient();
        client.connect(conf.getHost(), conf.getPort());
        if (!Strings.isBlank(conf.getUsername())) {
            if (!client.login(conf.getUsername(), conf.getToken())) {
                client.disconnect();
                return null;
            }
        }
        client.enterLocalPassiveMode();
        client.setFileType(FTP.BINARY_FILE_TYPE);
        return client;
    }

    public static String ftpPath(FtpConfig conf, String path) {
        if (!path.startsWith("/"))
            path = "/" + path;
        if (!Strings.isBlank(conf.getPathPrefix())) {
            path = conf.getPathPrefix() + path;
        }
        return path;
    }
    
    public static FtpConfig conf(String name, String user, WnIo io) {
        if ("root".equals(user)) {
            return conf(io.check(null, "/root/.ftp/" + name + "/ftpconf"), io);
        }
        return conf(io.check(null, "/home/"+user+"/.ftp/" + name + "/ftpconf"), io);
    }
    
    public static void invoke(FtpConfig conf, Callback<FTPClient> callback) {
        FTPClient client = null;
        try {
            client = FtpUtil.client(conf);
            callback.invoke(client);
        }
        catch (Throwable e) {
            log.debug(e.getMessage(), e);
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
}
