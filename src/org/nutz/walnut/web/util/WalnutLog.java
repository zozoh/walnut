package org.nutz.walnut.web.util;

import java.io.PrintStream;

import org.nutz.lang.Times;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.log.impl.AbstractLog;
import org.nutz.walnut.impl.box.JvmBoxOutput;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

public class WalnutLog extends AbstractLog {

    private static final String DFT_TMPL = "@{P} @{d<date:dd'T'HH:mm:ss.SSS>}: @{m}";

    protected WnSystem sys;

    protected Tmpl tmpl;

    public WalnutLog(WnSystem sys) {
        this.sys = sys;
    }

    public void setTmpl(String tmpl) {
        this.tmpl = Cmds.parse_tmpl(tmpl);
    }

    private String __msg(String lv, Object msg) {
        // 上下文
        NutMap map = new NutMap();
        map.put("p", lv);
        map.put("P", lv.substring(0, 1));
        map.put("d", Times.now());
        map.put("m", msg);

        // 渲染
        if (null == tmpl) {
            this.setTmpl(DFT_TMPL);
        }

        return tmpl.render(map, false);
    }

    public void setTmpl(Tmpl tmpl) {
        this.tmpl = tmpl;
    }

    public WalnutLog(WnSystem sys, int level) {
        this.sys = sys;
        setLevel(level);
    }

    public void fatal(Object message, Throwable t) {
        if (!isFatalEnabled())
            return;
        sys.err.println(__msg("FATAL", message));
        printThrowable(sys.err, t);
    }

    public void error(Object message, Throwable t) {
        if (!isErrorEnabled())
            return;
        sys.err.println(__msg("ERROR", message));
        printThrowable(sys.err, t);
    }

    public void warn(Object message, Throwable t) {
        if (!isWarnEnabled())
            return;
        sys.out.println(__msg("WARN", message));
        printThrowable(sys.out, t);
    }

    public void info(Object message, Throwable t) {
        if (!isInfoEnabled())
            return;
        sys.out.println(__msg("INFO", message));
        printThrowable(sys.out, t);
    }

    public void debug(Object message, Throwable t) {
        if (!isDebugEnabled())
            return;
        sys.out.println(__msg("DEBUG", message));
        printThrowable(sys.out, t);
    }

    public void trace(Object message, Throwable t) {
        if (!isWarnEnabled())
            return;
        sys.out.println(__msg("TRACE", message));
        printThrowable(sys.out, t);
    }
    
    public void printThrowable(JvmBoxOutput out, Throwable t) {
        if (t == null)
            return;
        t.printStackTrace(new PrintStream(out.getOutputStream(), true));
    }

    protected void log(int level, Object message, Throwable tx) {
        switch (level) {
        case LEVEL_FATAL:
            fatal(message, tx);
            break;
        case LEVEL_ERROR:
            error(message, tx);
            break;
        case LEVEL_WARN:
            warn(message, tx);
            break;
        case LEVEL_INFO:
            info(message, tx);
            break;
        case LEVEL_DEBUG:
            debug(message, tx);
            break;
        case LEVEL_TRACE:
            trace(message, tx);
            break;
        }
    }

    public void setLevel(int level) {
        this.isFatalEnabled = LEVEL_FATAL >= level;
        this.isErrorEnabled = LEVEL_ERROR >= level;
        this.isWarnEnabled = LEVEL_WARN >= level;
        this.isInfoEnabled = LEVEL_INFO >= level;
        this.isDebugEnabled = LEVEL_DEBUG >= level;
        this.isTraceEnabled = LEVEL_TRACE >= level;
    }

    public void asFatal() {
        this.setLevel(LEVEL_FATAL);
    }

    public void asError() {
        this.setLevel(LEVEL_ERROR);
    }

    public void asWarn() {
        this.setLevel(LEVEL_WARN);
    }

    public void asInfo() {
        this.setLevel(LEVEL_INFO);
    }

    public void asDebug() {
        this.setLevel(LEVEL_DEBUG);
    }

    public void asTrace() {
        this.setLevel(LEVEL_TRACE);
    }
}
