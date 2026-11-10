package com.site0.walnut.log;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * {@link WnLog} 的 log4j2 实现（RM-NUTZ-11）。
 * <p>
 * 行为对齐现有 {@code util.log.WnLogWrapper}：消息前缀 {@code (当前用户名)}，
 * 并遵守 {@code Wn.WC().isLogOff()} 的静默开关。日志后端直接用 log4j2，
 * 不再经过 {@code org.nutz.log}，为 RM-NUTZ-14 删除 Nutz 做准备。
 */
class WnLog4j implements WnLog {

    private final Logger log;

    WnLog4j(String name) {
        this.log = LogManager.getLogger(name);
    }

    @Override
    public WnLog setTag(String tag) {
        return this;
    }

    private boolean __off() {
        return Wn.WC().isLogOff();
    }

    private String __prefix() {
        String myName = Ws.sBlank(Wn.WC().getMyName(), "---");
        return "(" + myName + ")";
    }

    private Object __wrap(Object msg) {
        if (null == msg)
            return null;
        if (msg instanceof Throwable)
            return msg;
        return __prefix() + msg;
    }

    @Override
    public void log(int level, Object fmt, Object... args) {
        if (__off())
            return;
        String s = (fmt instanceof String) ? String.format((String) fmt, args)
            : String.valueOf(fmt);
        String msg = __prefix() + s;
        if (level >= WnLogs.LEVEL_FATAL) {
            log.fatal(msg);
        }
        else if (level >= WnLogs.LEVEL_ERROR) {
            log.error(msg);
        }
        else if (level >= WnLogs.LEVEL_WARN) {
            log.warn(msg);
        }
        else if (level >= WnLogs.LEVEL_INFO) {
            log.info(msg);
        }
        else if (level >= WnLogs.LEVEL_DEBUG) {
            log.debug(msg);
        }
        else {
            log.trace(msg);
        }
    }

    @Override
    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    @Override
    public void fatal(Object message) {
        log.fatal(__wrap(message));
    }

    @Override
    public void fatalf(String fmt, Object... args) {
        if (__off())
            return;
        log.fatal(__prefix() + String.format(fmt, args));
    }

    @Override
    public void fatal(Object message, Throwable t) {
        log.fatal(__wrap(message), t);
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void error(Object message) {
        log.error(__wrap(message));
    }

    @Override
    public void errorf(String fmt, Object... args) {
        if (__off())
            return;
        log.error(__prefix() + String.format(fmt, args));
    }

    @Override
    public void error(Object message, Throwable t) {
        log.error(__wrap(message), t);
    }

    @Override
    public boolean isWarnEnabled() {
        return !__off() && log.isWarnEnabled();
    }

    @Override
    public void warn(Object message) {
        if (__off())
            return;
        log.warn(__wrap(message));
    }

    @Override
    public void warnf(String fmt, Object... args) {
        if (__off())
            return;
        log.warn(__prefix() + String.format(fmt, args));
    }

    @Override
    public void warn(Object message, Throwable t) {
        if (__off())
            return;
        log.warn(__wrap(message), t);
    }

    @Override
    public boolean isInfoEnabled() {
        return !__off() && log.isInfoEnabled();
    }

    @Override
    public void info(Object message) {
        if (__off())
            return;
        log.info(__wrap(message));
    }

    @Override
    public void infof(String fmt, Object... args) {
        if (__off())
            return;
        log.info(__prefix() + String.format(fmt, args));
    }

    @Override
    public void info(Object message, Throwable t) {
        if (__off())
            return;
        log.info(__wrap(message), t);
    }

    @Override
    public boolean isDebugEnabled() {
        return !__off() && log.isDebugEnabled();
    }

    @Override
    public void debug(Object message) {
        if (__off())
            return;
        log.debug(__wrap(message));
    }

    @Override
    public void debugf(String fmt, Object... args) {
        if (__off())
            return;
        log.debug(__prefix() + String.format(fmt, args));
    }

    @Override
    public void debug(Object message, Throwable t) {
        if (__off())
            return;
        log.debug(__wrap(message), t);
    }

    @Override
    public boolean isTraceEnabled() {
        return !__off() && log.isTraceEnabled();
    }

    @Override
    public void trace(Object message) {
        if (__off())
            return;
        log.trace(__wrap(message));
    }

    @Override
    public void tracef(String fmt, Object... args) {
        if (__off())
            return;
        log.trace(__prefix() + String.format(fmt, args));
    }

    @Override
    public void trace(Object message, Throwable t) {
        if (__off())
            return;
        log.trace(__wrap(message), t);
    }

}
