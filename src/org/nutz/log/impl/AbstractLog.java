package org.nutz.log.impl;

import java.util.Arrays;

import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;

public abstract class AbstractLog implements Log {

    protected boolean isFatalEnabled = true;
    protected boolean isErrorEnabled = true;
    protected boolean isWarnEnabled = true;
    protected boolean isInfoEnabled = false;
    protected boolean isDebugEnabled = false;
    protected boolean isTraceEnabled = false;

    protected static final int LEVEL_FATAL = 50;
    protected static final int LEVEL_ERROR = 40;
    protected static final int LEVEL_WARN = 30;
    protected static final int LEVEL_INFO = 20;
    protected static final int LEVEL_DEBUG = 10;
    protected static final int LEVEL_TRACE = 0;

    public static int level(String str) {
        return level(str, false);
    }

    public static int level(String str, boolean strict) {
        if (null != str) {
            if ("F".equals(str) || "fatal".equals(str))
                return LEVEL_FATAL;

            if ("E".equals(str) || "error".equals(str))
                return LEVEL_ERROR;

            if ("W".equals(str) || "warn".equals(str))
                return LEVEL_WARN;

            if ("I".equals(str) || "info".equals(str))
                return LEVEL_INFO;

            if ("D".equals(str) || "debug".equals(str))
                return LEVEL_DEBUG;

            if ("T".equals(str) || "trace".equals(str))
                return LEVEL_TRACE;
        }
        if (strict) {
            throw Er.create("e.nutz.log.InvalidLogLevelStr", str);
        }
        return LEVEL_INFO;
    }

    protected abstract void doPrintLog(int level, Object message, Throwable tx);

    protected void printLog(int level, LogInfo info) {
        doPrintLog(level, info.message, info.e);
    }

    private static final LogInfo LOGINFO_ERROR = new LogInfo();
    private static final LogInfo LOGINFO_NULL = new LogInfo();
    static {
        LOGINFO_ERROR.message = "!!!!Log Fail!!";
        LOGINFO_NULL.message = "null";
    }

    /**
     * 产生一个LogInfo对象,以支持以下调用方式:
     * <p/>
     * <code>log.warn(e)</code>
     * <p/>
     * <code>log.warnf("User(name=%s) login fail",username,e)</code>
     */
    private LogInfo makeInfo(Object obj, Object... args) {
        if (obj == null)
            return LOGINFO_NULL;
        try {
            LogInfo info = new LogInfo();
            if (obj instanceof Throwable) {
                info.e = (Throwable) obj;
                info.message = info.e.getMessage();
            } else if (args == null || args.length == 0) {
                info.message = obj.toString();
            }
            // //map to another mehtod
            // else if (args.length == 1 && args[0] instanceof Throwable) {
            // info.message = obj.toString();
            // info.e = (Throwable)args[0];
            // }
            else {
                info.message = String.format(obj.toString(), args);
                if (args[args.length - 1] instanceof Throwable)
                    info.e = (Throwable) args[args.length - 1];
            }
            return info;
        }
        catch (Throwable e) { // 即使格式错误也继续log
            if (isWarnEnabled())
                warn("String format fail in log , fmt = "
                     + obj
                     + " , args = "
                     + Arrays.toString(args),
                     e);
            return LOGINFO_ERROR;
        }
    }

    public void log(int level, Object fmt, Object... args) {
        if (level == LEVEL_FATAL) {
            if (!isFatalEnabled())
                return;
        } else if (level == LEVEL_ERROR) {
            if (!isErrorEnabled())
                return;
        } else if (level == LEVEL_WARN) {
            if (!isWarnEnabled())
                return;
        } else if (level == LEVEL_INFO) {
            if (!isInfoEnabled())
                return;
        } else if (level == LEVEL_DEBUG) {
            if (!isDebugEnabled())
                return;
        } else if (level == LEVEL_TRACE) {
            if (!isTraceEnabled())
                return;
        } else {
            throw Er.create("e.nutz.log.InvalidLogLevel", level);
        }
        LogInfo info = makeInfo(fmt, args);
        printLog(level, info);
    }

    public void debug(Object message) {
        if (isDebugEnabled())
            printLog(LEVEL_DEBUG, makeInfo(message));
    }

    public void debugf(String fmt, Object... args) {
        if (isDebugEnabled())
            printLog(LEVEL_DEBUG, makeInfo(fmt, args));
    }

    public void error(Object message) {
        if (isErrorEnabled())
            printLog(LEVEL_ERROR, makeInfo(message));
    }

    public void errorf(String fmt, Object... args) {
        if (isErrorEnabled())
            printLog(LEVEL_ERROR, makeInfo(fmt, args));
    }

    public void fatal(Object message) {
        if (isFatalEnabled())
            printLog(LEVEL_FATAL, makeInfo(message));
    }

    public void fatalf(String fmt, Object... args) {
        if (isFatalEnabled())
            printLog(LEVEL_FATAL, makeInfo(fmt, args));
    }

    public void info(Object message) {
        if (isInfoEnabled())
            printLog(LEVEL_INFO, makeInfo(message));
    }

    public void infof(String fmt, Object... args) {
        if (isInfoEnabled())
            printLog(LEVEL_INFO, makeInfo(fmt, args));
    }

    public void trace(Object message) {
        if (isTraceEnabled())
            printLog(LEVEL_TRACE, makeInfo(message));
    }

    public void tracef(String fmt, Object... args) {
        if (isTraceEnabled())
            printLog(LEVEL_TRACE, makeInfo(fmt, args));
    }

    public void warn(Object message) {
        if (isWarnEnabled())
            printLog(LEVEL_WARN, makeInfo(message));
    }

    public void warnf(String fmt, Object... args) {
        if (isWarnEnabled())
            printLog(LEVEL_WARN, makeInfo(fmt, args));
    }

    public boolean isDebugEnabled() {
        return isDebugEnabled;
    }

    public boolean isErrorEnabled() {
        return isErrorEnabled;
    }

    public boolean isFatalEnabled() {
        return isFatalEnabled;
    }

    public boolean isInfoEnabled() {
        return isInfoEnabled;
    }

    public boolean isTraceEnabled() {
        return isTraceEnabled;
    }

    public boolean isWarnEnabled() {
        return isWarnEnabled;
    }

    protected String tag = "";

    public Log setTag(String tag) {
        this.tag = tag;
        return this;
    }
}
