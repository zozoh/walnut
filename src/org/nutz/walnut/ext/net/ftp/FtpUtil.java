package org.nutz.walnut.ext.net.ftp;

import java.io.IOException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.log.Log;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.util.Wn;

public class FtpUtil {
    
    private static final Log log = Wlog.getCMD();

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
    


    public static WnObj toWnObj(MimeMap mimes, FTPFile ftpFile, WnObj parent) {
        WnObj wobj = new WnIoObj();
        wobj.name(ftpFile.getName());
        if (parent.id().contains(":ftp:")) {
            wobj.id(parent.id() + "%" + wobj.name());
        } else {
            wobj.id(parent.id() + ":ftp:%%" + wobj.name());
        }
        wobj.mount(parent.mount() + "/" + wobj.name());
        wobj.data(wobj.mount());
        
        if (ftpFile.isDirectory()) {
            wobj.race(WnRace.DIR);
        }
        else {
            wobj.race(WnRace.FILE);
        }
        if(ftpFile.isSymbolicLink()) {
            wobj.link(ftpFile.getLink());
        }
        if (mimes != null)
            Wn.set_type(mimes, wobj, null);
        wobj.creator(parent.creator());
        wobj.group(parent.group());
        wobj.setParent(parent);
        
        wobj.mode(parent.mode());
        
        wobj.createTime(0);
        wobj.lastModified(0);
        wobj.len(ftpFile.getSize());
        wobj.mountRootId(parent.mountRootId());
        return wobj;
    }
}
