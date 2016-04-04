package org.nutz.walnut.ext.sshd;

import java.io.File;

import org.apache.sshd.common.session.Session.AttributeKey;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.WnRun;

@IocBean(create = "init", depose = "depose")
public class SshdServer extends WnRun {

    public static AttributeKey<WnSession> KEY_WN_SESSION = new AttributeKey<WnSession>();

    protected SshServer sshd = ServerBuilder.builder().build();

    public void start() throws Exception {
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("/tmp/hostkey.ser")));
        sshd.setPasswordAuthenticator((String username, String password, ServerSession session) -> {
            boolean re = usrs.checkPassword(username, password);
            if (re) {
                WnSession se = sess.create(usrs.check(username));
                session.setAttribute(KEY_WN_SESSION, se);
            }
            return re;
        });
        sshd.setShellFactory(() -> {
            return new WalnutSshdCommand(SshdServer.this);
        });
        // sshd.setCommandFactory(new CommandFactory() {
        // public Command createCommand(String command) {
        // return new WalnutSshdCommand(command);
        // }
        // });
        sshd.setPort(2222);
        sshd.start();
    }

    public void init() throws Exception {
        start();
    }

    public void depose() throws Exception {
        sshd.stop(true);
    }
}
