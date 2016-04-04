package org.nutz.walnut.ext.sshd;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.common.session.Session.AttributeKey;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.Wn;
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
            return new WalnutSshdCommand();
        });
        // sshd.setCommandFactory(new CommandFactory() {
        // public Command createCommand(String command) {
        // return new WalnutSshdCommand(command);
        // }
        // });
        sshd.setPort(2222);
        sshd.start();
    }

    public class WalnutSshdCommand implements Command, Runnable, SessionAware {

        private InputStream in;
        private OutputStream out;
        private OutputStream err;
        private ExitCallback callback;
        public boolean sayHi;
        protected Environment env;
        protected ServerSession session;
        protected WnSession se;

        public void start(Environment env) throws IOException {
            this.env = env;
            out.write("welcome to walnut\r\n".getBytes());
            out.flush();
            new Thread(this).start();
        }

        public void setOutputStream(OutputStream out) {
            this.out = out;
        }

        public void setInputStream(InputStream in) {
            this.in = in;
        }

        public void setExitCallback(ExitCallback callback) {
            this.callback = callback;
        }

        public void setErrorStream(OutputStream err) {
            this.err = err;
        }

        public void destroy() {}

        public void run() {
            try {
                String line;
                byte[] buf = new byte[1];
                int len;
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                printPH1();
                while (true) {
                    len = in.read(buf);
                    if (len == -1)
                        break;
                    if (len == 0)
                        continue;
                    out.write(buf[0]);
                    if (buf[0] == '\r') {
                        out.write('\n');
                        line = new String(bao.toByteArray()).trim();
                        bao.reset();
                    } else {
                        bao.write(buf[0]);
                        out.flush();
                        continue;
                    }
                    out.flush();
                    if ("exit".equals(line) || "quit".equals(line)) {
                        break;
                    }
                    if (!line.isEmpty())
                        execute(line);

                    printPH1();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            if (this.callback != null)
                this.callback.onExit(0);
        }

        public void printPH1() throws IOException {
            String line = String.format("%s:%s# ", se.me(), se.var("PWD"));
            out.write(line.getBytes());
            out.flush();
        }

        public void setSession(ServerSession session) {
            this.session = session;
            this.se = session.getAttribute(KEY_WN_SESSION);
        }

        public void execute(String cmdText) throws IOException {
            try {
                Wn.WC().SE(se);
                exec("",
                     se,
                     cmdText,
                     new NoCloseOutputStream(out),
                     new NoCloseOutputStream(err),
                     null,
                     null);
            }
            catch (Exception e) {
                out.write(("\r\nSystem ERR : " + e.getMessage() + "\r\n").getBytes());
            }
        }
    }

    public class NoCloseOutputStream extends FilterOutputStream {

        public NoCloseOutputStream(OutputStream out) {
            super(out);
        }

        public void close() throws IOException {}

        boolean preR;

        public void write(int b) throws IOException {
            switch (b) {
            case '\r':
                preR = true;
                break;
            case '\n':
                if (!preR) {
                    out.write('\r');
                }
                break;
            default:
                preR = false;
                break;
            }
            out.write(b);
        }
    }

    public void init() throws Exception {
        start();
    }

    public void depose() throws Exception {
        sshd.stop(true);
    }
}
