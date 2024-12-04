package com.site0.walnut.util.log;

import org.nutz.log.Log;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

public class WnLogWrapper implements Log {

    private Log log;

    public WnLogWrapper(Log log) {
        this.log = log;
    }

    public Log setTag(String tag) {
        return log.setTag(tag);
    }

    private String _prefix() {
        String myName = Ws.sBlank(Wn.WC().getMyName(), "---");
        return "(" + myName + ")";
    }

    public Object _wrap_msg(Object msg) {
        if (null == msg) {
            return null;
        }
        if (msg instanceof Throwable) {
            return msg;
        }
        return _prefix() + msg;
    }

    @Override
    public void log(int level, Object fmt, Object... args) {
        log.log(level, fmt, args);
    }

    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    public void fatal(Object message) {
        log.fatal(_wrap_msg(message));
    }

    public void fatalf(String fmt, Object... args) {
        log.fatalf(_prefix() + fmt, args);
    }

    public void fatal(Object message, Throwable t) {
        log.fatal(_wrap_msg(message), t);
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public void error(Object message) {
        log.error(_wrap_msg(message));
    }

    public void errorf(String fmt, Object... args) {
        log.errorf(_prefix() + fmt, args);
    }

    public void error(Object message, Throwable t) {
        log.error(_wrap_msg(message), t);
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    public void warn(Object message) {
        log.warn(_wrap_msg(message));
    }

    public void warnf(String fmt, Object... args) {
        log.warnf(_prefix() + fmt, args);
    }

    public void warn(Object message, Throwable t) {
        log.warn(_wrap_msg(message), t);
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public void info(Object message) {
        log.info(_wrap_msg(message));
    }

    public void infof(String fmt, Object... args) {
        log.infof(_prefix() + fmt, args);
    }

    public void info(Object message, Throwable t) {
        log.info(_wrap_msg(message), t);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public void debug(Object message) {
        log.debug(_wrap_msg(message));
    }

    public void debugf(String fmt, Object... args) {
        log.debugf(_prefix() + fmt, args);
    }

    public void debug(Object message, Throwable t) {
        log.debug(_wrap_msg(message), t);
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public void trace(Object message) {
        log.trace(_wrap_msg(message));
    }

    public void tracef(String fmt, Object... args) {
        log.tracef(_prefix() + fmt, args);
    }

    public void trace(Object message, Throwable t) {
        log.trace(_wrap_msg(message), t);
    }

}
