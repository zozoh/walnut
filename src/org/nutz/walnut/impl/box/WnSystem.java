package org.nutz.walnut.impl.box;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;
import org.nutz.walnut.web.util.WalnutLog;

public class WnSystem {

    private static final Log log = Logs.get();

    public String boxId;

    public int pipeId;

    public int nextId;

    public boolean isOutRedirect;

    public String cmdOriginal;

    public WnUsr me;

    public WnSession se;

    public JvmBoxInput in;

    public JvmBoxOutput out;

    public JvmBoxOutput err;

    public WnIo io;

    public WnSessionService sessionService;

    public WnUsrService usrService;

    public JvmExecutorFactory jef;

    JvmAtomRunner _runner;

    public NutMap attrs() {
        return _runner.bc.attrs;
    }

    public void exec(String cmdText) {
        exec(cmdText, out.getOutputStream(), err.getOutputStream(), in.getInputStream());
    }

    public void execf(String fmt, Object... args) {
        String cmdText = String.format(fmt, args);
        exec(cmdText);
    }

    public void exec(String cmdText, OutputStream stdOut, OutputStream stdErr, InputStream stdIn) {
        String[] cmdLines = Cmds.splitCmdLine(cmdText);
        _runner.out = new EscapeCloseOutputStream(null == stdOut ? out.getOutputStream() : stdOut);
        _runner.err = new EscapeCloseOutputStream(null == stdErr ? err.getOutputStream() : stdErr);
        _runner.in = new EscapeCloseInputStream(null == stdIn ? in.getInputStream() : stdIn);

        if (log.isInfoEnabled())
            log.info(" > sys.exec: " + cmdText);

        for (String cmdLine : cmdLines) {
            _runner.run(cmdLine);
            _runner.wait_for_idle();
        }
        _runner.__free();
    }

    public void exec(String cmdText,
                     StringBuilder stdOut,
                     StringBuilder stdErr,
                     CharSequence stdIn) {
        InputStream ins = null == stdIn ? in.getInputStream() : Lang.ins(stdIn);
        OutputStream out = null == stdOut ? null : Lang.ops(stdOut);
        OutputStream err = null == stdErr ? null : Lang.ops(stdErr);

        exec(cmdText, out, err, ins);
    }

    public void exec(String cmdText, CharSequence input) {
        exec(cmdText, null, null, input);
    }

    public String exec2(String cmdText) {
        return exec2(cmdText, null);
    }

    public String exec2f(String fmt, Object... args) {
        String cmdText = String.format(fmt, args);
        return exec2(cmdText);
    }

    public String exec2(String cmdText, CharSequence input) {
        StringBuilder sb = new StringBuilder();
        exec(cmdText, sb, sb, input);
        return sb.toString();
    }

    public Log getLog(ZParams params) {
        WalnutLog log = new WalnutLog(this, params.is("v") ? 10 : 40);
        // 设置级别（默认info)
        if (params.is("warn")) {
            log.asWarn();
        } else if (params.is("debug")) {
            log.asDebug();
        } else if (params.is("v") || params.is("trace")) {
            log.asTrace();
        } else {
            log.asInfo();
        }
        return log;
    }
}
