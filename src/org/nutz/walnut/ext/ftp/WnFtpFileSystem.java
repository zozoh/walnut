package org.nutz.walnut.ext.ftp;

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.util.Wn;

public class WnFtpFileSystem implements FileSystemView {
    
    protected WnIo io;
    protected WnObj homeDir;
    protected WnObj currentDir;
    protected WnUsr u;

    public WnFtpFileSystem(WnIo io, WnUsr u, WnObj home) {
        super();
        this.io = io;
        this.u = u;
        homeDir = home;
        currentDir = homeDir;
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException {
        return new WnFtpFile(u, io, homeDir, null);
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        return new WnFtpFile(u, io, currentDir, null);
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
        return new WnFtpFile(u, io, io.fetch(currentDir, file), Wn.appendPath(currentDir.path(), file));
    }

    public boolean isRandomAccessible() throws FtpException {
        return true;
    }

    @Override
    public void dispose() {}

    

}
