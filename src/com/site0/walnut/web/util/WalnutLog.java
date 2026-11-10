package com.site0.walnut.web.util;

import java.io.PrintStream;
import java.util.Date;

import com.site0.walnut.log.WnLog;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmBoxOutput;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;

/**
 * 命令日志输出（写到当前盒子的 stdout/stderr）。
 * <p>
 * RM-NUTZ-13：改为实现 {@link WnLog}，不再继承 Nutz {@code AbstractLog}。
 */
public class WalnutLog implements WnLog {

    private static final int LEVEL_FATAL = 50;

    private static final int LEVEL_ERROR = 40;

    private static final int LEVEL_WARN = 30;

    private static final int LEVEL_INFO = 20;

    private static final int LEVEL_DEBUG = 10;

    private static final int LEVEL_TRACE = 0;

    private static final String DFT_TMPL = "@{P} @{d<date:dd'T'HH:mm:ss.SSS>}: @{m}";

    protected WnSystem sys;

    protected WnTmpl tmpl;

    private boolean fatalEnabled;

    private boolean errorEnabled;

    private boolean warnEnabled;

    private boolean infoEnabled;

    private boolean debugEnabled;

    private boolean traceEnabled;

    public WalnutLog(WnSystem sys) {
        this.sys = sys;
    }

    public WalnutLog(WnSystem sys, int level) {
        this.sys = sys;
        setLevel(level);
    }

    public void setTmpl(String tmpl) {
        this.tmpl = Cmds.parse_tmpl(tmpl);
    }

    public void setTmpl(WnTmpl tmpl) {
        this.tmpl = tmpl;
    }

    private String __msg(String lv, Object msg) {
        NutMap map = new NutMap();
        map.put("p", lv);
        map.put("P", lv.substring(0, 1));
        map.put("d", new Date());
        map.put("m", msg);

        if (null == tmpl) {
            this.setTmpl(DFT_TMPL);
        }
        return tmpl.render(map, false);
    }

    public void printThrowable(JvmBoxOutput out, Throwable t) {
        if (t == null) {
            return;
        }
        t.printStackTrace(new PrintStream(out.getOutputStream(), true));
    }

    public void setLevel(int level) {
        this.fatalEnabled = LEVEL_FATAL >= level;
        this.errorEnabled = LEVEL_ERROR >= level;
        this.warnEnabled = LEVEL_WARN >= level;
        this.infoEnabled = LEVEL_INFO >= level;
        this.debugEnabled = LEVEL_DEBUG >= level;
        this.traceEnabled = LEVEL_TRACE >= level;
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

    @Override
    public WnLog setTag(String tag) {
        return this;
    }

    @Override
    public void log(int level, Object fmt, Object... args) {
        String msg = (fmt instanceof String) ? String.format((String) fmt, args)
            : String.valueOf(fmt);
        if (level >= LEVEL_FATAL) {
            fatal(msg);
        }
        else if (level >= LEVEL_ERROR) {
            error(msg);
        }
        else if (level >= LEVEL_WARN) {
            warn(msg);
        }
        else if (level >= LEVEL_INFO) {
            info(msg);
        }
        else if (level >= LEVEL_DEBUG) {
            debug(msg);
        }
        else {
            trace(msg);
        }
    }

    @Override
    public boolean isFatalEnabled() {
        return fatalEnabled;
    }

    @Override
    public void fatal(Object message) {
        fatal(message, null);
    }

    @Override
    public void fatalf(String fmt, Object... args) {
        fatal(String.format(fmt, args));
    }

    @Override
    public void fatal(Object message, Throwable t) {
        if (!isFatalEnabled())
            return;
        sys.err.println(__msg("FATAL", message));
        printThrowable(sys.err, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return errorEnabled;
    }

    @Override
    public void error(Object message) {
        error(message, null);
    }

    @Override
    public void errorf(String fmt, Object... args) {
        error(String.format(fmt, args));
    }

    @Override
    public void error(Object message, Throwable t) {
        if (!isErrorEnabled())
            return;
        sys.err.println(__msg("ERROR", message));
        printThrowable(sys.err, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return warnEnabled;
    }

    @Override
    public void warn(Object message) {
        warn(message, null);
    }

    @Override
    public void warnf(String fmt, Object... args) {
        warn(String.format(fmt, args));
    }

    @Override
    public void warn(Object message, Throwable t) {
        if (!isWarnEnabled())
            return;
        sys.out.println(__msg("WARN", message));
        printThrowable(sys.out, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return infoEnabled;
    }

    @Override
    public void info(Object message) {
        info(message, null);
    }

    @Override
    public void infof(String fmt, Object... args) {
        info(String.format(fmt, args));
    }

    @Override
    public void info(Object message, Throwable t) {
        if (!isInfoEnabled())
            return;
        sys.out.println(__msg("INFO", message));
        printThrowable(sys.out, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    @Override
    public void debug(Object message) {
        debug(message, null);
    }

    @Override
    public void debugf(String fmt, Object... args) {
        debug(String.format(fmt, args));
    }

    @Override
    public void debug(Object message, Throwable t) {
        if (!isDebugEnabled())
            return;
        sys.out.println(__msg("DEBUG", message));
        printThrowable(sys.out, t);
    }

    @Override
    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    @Override
    public void trace(Object message) {
        trace(message, null);
    }

    @Override
    public void tracef(String fmt, Object... args) {
        trace(String.format(fmt, args));
    }

    @Override
    public void trace(Object message, Throwable t) {
        if (!isTraceEnabled())
            return;
        sys.out.println(__msg("TRACE", message));
        printThrowable(sys.out, t);
    }
}
