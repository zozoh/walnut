package org.nutz.walnut.impl.box;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnSessionService;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;

public class WnSystem {

    public int pipeId;

    public int nextId;

    public String original;

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

    public void exec(String cmdText) {
        String[] cmdLines = Jvms.split(cmdText, true, '\n', ';');
        _runner.out = new EscapeCloseOutputStream(out.getOutputStream());
        _runner.err = new EscapeCloseOutputStream(err.getOutputStream());
        _runner.in = new EscapeCloseInputStream(in.getInputStream());
        for (String cmdLine : cmdLines) {
            _runner.__run(cmdLine);
            _runner.__wait_for_idle();
        }
        _runner.__free();
    }

}
