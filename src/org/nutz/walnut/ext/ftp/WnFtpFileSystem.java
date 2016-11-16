package org.nutz.walnut.ext.ftp;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;

public class WnFtpFileSystem implements FileSystemView {
    
    protected WnRun run;
    protected WnObj homeDir;
    protected WnObj currentDir;
    protected WnSession session;

    public WnFtpFileSystem(WnRun run, WnSession session) {
        super();
        this.run = run;
        this.session = session;
        homeDir = run.io().check(null, Wn.normalizeFullPath("~/", session));
        currentDir = homeDir;
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException {
        return new WnFtpFile(run, homeDir, null);
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        return new WnFtpFile(run, currentDir, null);
    }

    @Override
    public boolean changeWorkingDirectory(String dir) throws FtpException {
        WnObj newCwd = run.io().fetch(null, dir);
        if (newCwd == null || !newCwd.isDIR())
            return false;
        currentDir = newCwd;
        return true;
    }

    public FtpFile getFile(String file) throws FtpException {
        return new WnFtpFile(run, run.io().fetch(currentDir, file), Wn.appendPath(currentDir.path(), file));
    }

    public boolean isRandomAccessible() throws FtpException {
        return true;
    }

    @Override
    public void dispose() {}

    

}
