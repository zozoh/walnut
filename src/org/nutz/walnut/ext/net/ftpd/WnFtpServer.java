package org.nutz.walnut.ext.net.ftpd;

import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.listener.ListenerFactory;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.WnRun;

@IocBean(create = "start", depose = "stop")
public class WnFtpServer {

    protected FtpServer server;

    @Inject
    protected WnRun wnRun;

    @Inject
    protected WnFtpUserManager wnFtpUserManager;

    @Inject
    protected PropertiesProxy conf;

    protected int port;

    public boolean start() throws Exception {
        if (isRunning())
            return true;
        if (port < 1) {
            port = conf.getInt("ftp-port", -1);
        }
        if (port < 1)
            return false;
        FtpServerFactory serverFactory = new FtpServerFactory();
        serverFactory.setFileSystem(new FileSystemFactory() {
            public FileSystemView createFileSystemView(User user) throws FtpException {
                WnAccount u = wnRun.auth().checkAccount(user.getName());
                WnObj oHome = wnRun.io().check(null, user.getHomeDirectory());
                return new WnFtpFileSystem(wnRun, u, oHome);
            }
        });
        serverFactory.setUserManager(wnFtpUserManager);
        ListenerFactory lf = new ListenerFactory();
        lf.setPort(port);
        DataConnectionConfigurationFactory dccf = new DataConnectionConfigurationFactory();
        dccf.setPassivePorts(conf.get("ftp-passive-port", "" + (port + 10000)));
        lf.setDataConnectionConfiguration(dccf.createDataConnectionConfiguration());
        serverFactory.addListener("default", lf.createListener());
        server = serverFactory.createServer();
        // start the server
        server.start();
        return true;
    }

    public void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    public boolean isRunning() {
        return server != null && !server.isStopped();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
