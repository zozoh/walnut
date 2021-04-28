package org.nutz.walnut.ext.net.ftpd;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;
import org.nutz.lang.Lang;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;

public class WnFtpFile implements FtpFile {
    
    protected WnObj wobj;
    protected String path;
    protected WnFtpFileSystem fs;

    public WnFtpFile(WnFtpFileSystem fs, WnObj wobj, String path) {
        this.fs = fs;
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
        fs.su(()->{
            wobj.lastModified(time);
            fs.io.appendMeta(wobj, "^lm$");
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
        fs.su(()->
            fs.io.createIfNoExists(null, path, WnRace.DIR)
        );
        return true;
    }

    @Override
    public boolean delete() {
        fs.su(()->
            fs.io.delete(wobj)
        );
        return true;
    }

    @Override
    public boolean move(FtpFile destination) {
        fs.su(()->
            fs.io.move(wobj, destination.getAbsolutePath())
        );
        return true;
    }

    @Override
    public List<? extends FtpFile> listFiles() {
        List<FtpFile> files = new ArrayList<>();
        if (wobj.isDIR()) {
            fs.su(()-> {
                List<WnObj> objs = fs.io.query(Wn.Q.pid(wobj));
                for (WnObj obj : objs) {
                    files.add(fs.getFile(obj, null));
                }
            });
        }
        return files;
    }

    public OutputStream createOutputStream(long offset) throws IOException {
        return fs.su2(new Proton<OutputStream>() {
            protected OutputStream exec() {
                if (wobj == null)
                    wobj = fs.io.create(null, path, WnRace.FILE);
                final OutputStream out = fs.io.getOutputStream(wobj, offset);
                return new FilterOutputStream(out) {
                    public void close() throws IOException {
                        fs.su2(new Proton<Object>() {
                            protected Object exec() {
                                try {
                                    out.close();
                                }
                                catch (IOException e) {
                                    throw Lang.wrapThrow(e);
                                }
                                return null;
                            }
                        });
                    }
                };
            }
        });
    }

    public InputStream createInputStream(long offset) throws IOException {
        return fs.su2(new Proton<InputStream>() {
            protected InputStream exec() {
                return fs.io.getInputStream(wobj, offset);
            }
        });
    }
}
