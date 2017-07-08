package org.nutz.walnut.ext.ftp.mount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPFile;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.MimeMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.ftp.FtpConfig;
import org.nutz.walnut.ext.ftp.FtpUtil;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.impl.io.mnt.AbstractWnMounter;
import org.nutz.walnut.util.Wn;
import org.nutz.web.Webs.Err;

public class FtpMounter extends AbstractWnMounter {
    
    private static final Log log = Logs.get();
    
    protected static boolean DEBUG = true;
    
    @Inject
    protected WnIo io;

    @Override
    public WnObj get(MimeMap mimes, WnObj mo, String[] paths, int fromIndex, int toIndex) {
        if (DEBUG)
            log.debugf("%s : %s %d %d", mo.mount(), Json.toJson(paths), fromIndex, toIndex);
        String[] tmp = mo.mount().substring("ftp://".length()).split("/", 2);
        String confName = tmp[0];
        FtpConfig conf = FtpUtil.conf(confName, mo.creator(), io);
        String path = Strings.join("/", Arrays.copyOfRange(paths, fromIndex, toIndex - 1));
        path = FtpUtil.ftpPath(conf, path);
        String _path = path;
        WnObj[] re = new WnObj[1];
        FtpUtil.invoke(conf, client -> {
            try {
                for (FTPFile f : client.listFiles(_path)) {
                    if (f.getName().equals(paths[toIndex - 1])) {
                        re[0] = toWnObj(mimes, f, mo);
                        String p = Strings.join("%", Arrays.copyOfRange(paths, fromIndex, toIndex));
                        re[0].id(mo.id() + ":ftp:%%" + p);
                        re[0].mount(mo.mount() + "/" + p);
                        re[0].data(re[0].mount());
                        re[0].setv("ph", mo.path() + "/" + p.replace('%', '/'));
                        break;
                    }
                };
            }
            catch (IOException e) {
                throw Err.create("e.mount.ftp.list_error");
            }
        });
        return re[0];
    }

    @Override
    public List<WnObj> getChildren(MimeMap mimes, WnObj mo, String name) {
        if (DEBUG)
            log.debugf("%s : %s", mo.mount() , name);
        List<WnObj> list = new ArrayList<>();
        String[] tmp = mo.mount().substring("ftp://".length()).split("/", 2);
        String confName = tmp[0];
        String path = "/";
        if (tmp.length > 1) {
            path += tmp[1].replace('%', '/');
        }
        Pattern pattern = namePatten(name);
        FtpConfig conf = FtpUtil.conf(confName, mo.creator(), io);
        path = FtpUtil.ftpPath(conf, path);
        String _path = path;
        FtpUtil.invoke(conf, client -> {
            try {
                for (FTPFile f : client.listFiles(_path)) {
                    if (pattern == null || pattern.matcher(f.getName()).find())
                        list.add(toWnObj(mimes, f, mo));
                };
            }
            catch (IOException e) {
                throw Err.create("e.mount.ftp.list_error");
            }
        });
        
        return list;
    }

    public static WnObj toWnObj(MimeMap mimes, FTPFile ftpFile, WnObj parent) {
        WnObj wobj = new WnBean();
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
