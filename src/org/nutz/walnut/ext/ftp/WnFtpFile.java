package org.nutz.walnut.ext.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.io.WnEvalLink;
import org.nutz.walnut.util.Wn;

public class WnFtpFile implements FtpFile {
    
    protected WnIo io;
    protected WnObj wobj;
    protected String path;
    protected WnUsr u;

    public WnFtpFile(WnUsr u, WnIo io, WnObj wobj, String path) {
        this.u = u;
        this.io = io;
        this.wobj = wobj;
        this.path = path;
        if (this.wobj != null)
            this.path = wobj.path();
    }

    @Override
    public String getAbsolutePath() {
        return wobj == null ? path : wobj.path();
    }

    @Override
    public String getName() {
        return wobj.name();
    }

    @Override
    public boolean isHidden() {
        return wobj.isHidden();
    }

    @Override
    public boolean isDirectory() {
        return wobj != null && wobj.isDIR();
    }

    @Override
    public boolean isFile() {
        return wobj != null && wobj.isFILE();
    }

    @Override
    public boolean doesExist() {
        return wobj != null;
    }

    @Override
    public boolean isReadable() {
        return true;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isRemovable() {
        return true;
    }

    @Override
    public String getOwnerName() {
        return wobj.creator();
    }

    @Override
    public String getGroupName() {
        return wobj.group();
    }

    @Override
    public int getLinkCount() {
        return 0;
    }

    @Override
    public long getLastModified() {
        return wobj.lastModified();
    }

    @Override
    public boolean setLastModified(long time) {
        su(()->{
            wobj.lastModified(time);
            io.appendMeta(wobj, "^lm$");
        });
        return true;
    }

    @Override
    public long getSize() {
        return wobj.len();
    }

    @Override
    public Object getPhysicalFile() {
        return null;
    }

    @Override
    public boolean mkdir() {
        su(()->
            io.createIfNoExists(null, path, WnRace.DIR)
        );return true;
    }

    @Override
    public boolean delete() {
        su(()->
            io.delete(wobj)
        );
        return true;
    }

    @Override
    public boolean move(FtpFile destination) {
        su(()->
            io.move(wobj, destination.getAbsolutePath())
        );
        return true;
    }

    @Override
    public List<? extends FtpFile> listFiles() {
        List<WnFtpFile> files = new ArrayList<>();
        if (wobj.isDIR()) {
            su(()-> {
                List<WnObj> objs = io.query(Wn.Q.pid(wobj));
                for (WnObj obj : objs) {
                    files.add(new WnFtpFile(u, io, obj, null));
                }
            });
        }
        return files;
    }

    public OutputStream createOutputStream(long offset) throws IOException {
        return su2(new Proton<OutputStream>() {
            protected OutputStream exec() {
                return io.getOutputStream(wobj, offset);
            }
        });
    }

    public InputStream createInputStream(long offset) throws IOException {
        return su2(new Proton<InputStream>() {
            protected InputStream exec() {
                return io.getInputStream(wobj, offset);
            }
        });
    }

    protected void su(Atom atom) {
        Wn.WC().security(new WnEvalLink(io), ()->Wn.WC().su(u, atom));
    }
    
    protected <T> T su2(Proton<T> proton) {
        return Wn.WC().security(new WnEvalLink(io), new Proton<T>() {
            protected T exec() {
                return Wn.WC().su(u, proton);
            }
        });
    }
}
