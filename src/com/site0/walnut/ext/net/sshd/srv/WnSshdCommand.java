package com.site0.walnut.ext.net.sshd.srv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.session.ServerSession;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnRun;

public class WnSshdCommand implements Command, Runnable, SessionAware {

    private InputStream in;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    protected Environment env;
    protected ServerSession session;
    protected WnAuthSession se;
    protected WnRun run;
    protected String cmd;

    public WnSshdCommand(WnRun run) {
        this.run = run;
    }

    public WnSshdCommand(WnRun run, String cmd) {
        this.run = run;
        this.cmd = cmd;
    }

    public void start(Environment env) throws IOException {
        this.env = env;
        if (cmd == null) {
            out.write("welcome to walnut\r\n".getBytes());
            out.flush();
        }
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
            if (cmd == null) {
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
            } else {
                execute(cmd);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (this.callback != null)
                this.callback.onExit(0);
        }
    }

    public void printPH1() throws IOException {
        WnAccount me = se.getMe();
        String pwd = se.getVars().getString("PWD");
        String line = String.format("%s:%s# ", me.getName(), pwd);
        out.write(line.getBytes());
        out.flush();
    }

    public void setSession(ServerSession session) {
        this.session = session;
        this.se = session.getAttribute(WnSshd.KEY_WN_SESSION);
    }

    public void execute(String cmdText) throws IOException {
        try {
            Wn.WC().setSession(se);
            run.exec("",
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
