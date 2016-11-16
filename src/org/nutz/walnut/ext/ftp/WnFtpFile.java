package org.nutz.walnut.ext.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.ftpserver.ftplet.FtpFile;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;

public class WnFtpFile implements FtpFile {
    
    protected WnRun run;
    protected WnObj wobj;
    protected String path;

    public WnFtpFile(WnRun run, WnObj wobj, String path) {
        super();
        this.run = run;
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
        wobj.lastModified(time);
        run.io().appendMeta(wobj, "^lm$");
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
        run.io().createIfNoExists(null, path, WnRace.DIR);
        return true;
    }

    @Override
    public boolean delete() {
        run.io().delete(wobj);
        return true;
    }

    @Override
    public boolean move(FtpFile destination) {
        run.io().move(wobj, destination.getAbsolutePath());
        return true;
    }

    @Override
    public List<? extends FtpFile> listFiles() {
        List<WnFtpFile> files = new ArrayList<>();
        if (wobj.isDIR()) {
            List<WnObj> objs = run.io().query(Wn.Q.pid(wobj));
            for (WnObj obj : objs) {
                files.add(new WnFtpFile(run, obj, null));
            }
        }
        return files;
    }

    public OutputStream createOutputStream(long offset) throws IOException {
        if (wobj == null)
            wobj = run.io().createIfNoExists(null, path, WnRace.FILE);
        return run.io().getOutputStream(wobj, offset);
    }

    public InputStream createInputStream(long offset) throws IOException {
        return run.io().getInputStream(wobj, offset);
    }

}
