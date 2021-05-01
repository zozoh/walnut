package org.nutz.walnut.ext.net.sshd.srv;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.config.keys.AuthorizedKeyEntry;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.WnRun;

@IocBean(create = "init", depose = "depose")
public class WnSshdServer extends WnRun {

    protected SshServer sshd = ServerBuilder.builder().build();

    @Inject
    protected PropertiesProxy conf;

    protected int port;

    public void start() throws Exception {
        if (port < 1)
            return;
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("/tmp/hostkey.ser")));
        sshd.setPasswordAuthenticator((String username, String password, ServerSession session) -> {
            WnAccount usr = auth().getAccount(username);
            if (usr == null)
                return false;
            boolean re = usr.isMatchedRawPasswd(password);
            if (!re) {
                String aph = usr.getHomePath() + "/.ssh/token";
                WnObj wobj = io().fetch(null, aph);
                if (wobj != null && wobj.isFILE()) {
                    String token = io().readText(wobj);
                    re = password.equals(token);
                }
            }
            if (re) {
                WnAuthSession se = auth().createSession(usr, true);
                session.setAttribute(WnSshd.KEY_WN_SESSION, se);
            }
            return re;
        });
        sshd.setPublickeyAuthenticator((username, key, session) -> {
            WnAccount usr = auth().getAccount(username);
            if (usr == null)
                return false;
            String aph = usr.getHomePath() + "/.ssh/authorized_keys";
            WnObj wobj = io().fetch(null, aph);
            if (wobj != null && wobj.isFILE() && wobj.len() > 64) {
                String authorized_keys = io().readText(wobj);
                try {
                    List<AuthorizedKeyEntry> entries = AuthorizedKeyEntry.readAuthorizedKeys(new StringReader(authorized_keys),
                                                                                             false);
                    boolean re = AuthorizedKeyEntry.fromAuthorizedEntries(PublicKeyEntryResolver.IGNORING,
                                                                          entries)
                                                   .authenticate(username, key, session);
                    if (re) {
                        WnAuthSession se = auth().createSession(usr, true);
                        session.setAttribute(WnSshd.KEY_WN_SESSION, se);
                    }
                    return re;
                }
                catch (Throwable e) {}
            }
            return false;
        });
        sshd.setShellFactory(() -> {
            return new WnSshdCommand(WnSshdServer.this);
        });
        sshd.setCommandFactory((cmd) -> {
            return new WnSshdCommand(WnSshdServer.this, cmd);
        });
        sshd.setFileSystemFactory((session) -> {
            try {
                return new WnJdkFileSystemProvider(session, io()).getFileSystem(new URI("/"));
            }
            catch (URISyntaxException e) {
                throw Lang.impossible();
            }
        });
        sshd.setSubsystemFactories(Arrays.<NamedFactory<Command>> asList(new SftpSubsystemFactory()));
        sshd.setPort(port);
        sshd.start();
    }

    public void stop() throws IOException {
        if (sshd != null)
            sshd.stop(true);
    }

    public void init() throws Exception {
        port = conf.getInt("sshd-port", -1);
        start();
    }

    public boolean isRunning() {
        return sshd != null && sshd.isOpen();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void depose() throws Exception {
        stop();
    }
}
