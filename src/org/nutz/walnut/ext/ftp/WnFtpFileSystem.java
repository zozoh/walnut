package org.nutz.walnut.ext.ftp;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.WnRun;

public class WnFtpFileSystem implements FileSystemView {
    
    protected WnIo io;
    protected WnObj homeDir;
    protected WnObj currentDir;
    protected WnUsr u;
    protected WnUsrService usrs;
    protected WnRun run;
    
    public WnFtpFileSystem(WnRun run, WnUsr u, WnObj home) {
        super();
        this.run = run;
        this.io = run.io();
        this.usrs = run.usrs();
        this.u = u;
        homeDir = home;
        currentDir = homeDir;
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException {
        return getFile(homeDir, null);
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        return getFile(currentDir, null);
    }

    @Override
    public boolean changeWorkingDirectory(String dir) throws FtpException {
        WnObj newCwd = io.fetch(currentDir, dir);
        if (newCwd == null || !newCwd.isDIR())
            return false;
        currentDir = newCwd;
        return true;
    }

    public FtpFile getFile(String file) throws FtpException {
        if (file.startsWith("/")) {
            // pass
        } else if (file.startsWith("./")) {
            file = currentDir.path() + file.substring(1);
        } else {
            file = currentDir.path() + "/" + file;
        }
        return getFile(io.fetch(null, file), file);
    }
    
    public FtpFile getFile(WnObj obj, String file) {
        return new WnFtpFile(this, obj, obj == null ? file : obj.path());
    }

    public boolean isRandomAccessible() throws FtpException {
        return true;
    }

    @Override
    public void dispose() {}

    protected void su(Atom atom) {
        run.runWithHook(u, u.group(), null, (session) -> atom.run());
    }
    
    protected <T> T su2(Proton<T> proton) {
        run.runWithHook(u, u.group(), null, (session) -> proton.run());
        return proton.get();
    }

}
