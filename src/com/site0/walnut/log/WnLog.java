package com.site0.walnut.log;

/**
 * Walnut 自有日志接口（RM-NUTZ-11）。
 * <p>
 * 方法签名与 {@code com.site0.walnut.log.WnLog} 对齐（debug/info/warn/error/fatal + 各 {@code *f}
 * 格式化变体 + {@code isXxxEnabled} + {@code setTag}），供 RM-NUTZ-12 把全库
 * {@code com.site0.walnut.log.WnLog} 机械替换为 {@link WnLog}。
 * <p>
 * 实现见 {@link WnLog4j}，工厂见 {@link WnLogs}；本任务不改造日志采集/输出格式。
 */
public interface WnLog {

    WnLog setTag(String tag);

    void log(int level, Object fmt, Object... args);

    boolean isFatalEnabled();

    void fatal(Object message);

    void fatalf(String fmt, Object... args);

    void fatal(Object message, Throwable t);

    boolean isErrorEnabled();

    void error(Object message);

    void errorf(String fmt, Object... args);

    void error(Object message, Throwable t);

    boolean isWarnEnabled();

    void warn(Object message);

    void warnf(String fmt, Object... args);

    void warn(Object message, Throwable t);

    boolean isInfoEnabled();

    void info(Object message);

    void infof(String fmt, Object... args);

    void info(Object message, Throwable t);

    boolean isDebugEnabled();

    void debug(Object message);

    void debugf(String fmt, Object... args);

    void debug(Object message, Throwable t);

    boolean isTraceEnabled();

    void trace(Object message);

    void tracef(String fmt, Object... args);

    void trace(Object message, Throwable t);

}
