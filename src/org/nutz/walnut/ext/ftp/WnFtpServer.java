package org.nutz.walnut.ext.ftp;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.listener.ListenerFactory;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.walnut.util.WnRun;

@IocBean(create = "init", depose = "depose")
public class WnFtpServer {

    protected FtpServer server;
    
    @Inject
    protected WnRun wnRun;
    
    @Inject
    protected WnFtpUserManager wnFtpUserManager;
    
    public void init() throws Exception {
        FtpServerFactory serverFactory = new FtpServerFactory();
        serverFactory.setFileSystem(new FileSystemFactory() {
            public FileSystemView createFileSystemView(User user) throws FtpException {
                return new WnFtpFileSystem(wnRun, wnRun.creatSession(user.getName()));
            }
        });
        serverFactory.setUserManager(wnFtpUserManager);
        ListenerFactory lf = new ListenerFactory();
        lf.setPort(2121);
        serverFactory.addListener("default", lf.createListener());
        server = serverFactory.createServer();
        // start the server
        server.start();
    }
    
    public void depose() {
        if (server != null)
            server.stop();
    }
}
