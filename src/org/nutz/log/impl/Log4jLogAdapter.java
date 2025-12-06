package org.nutz.log.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.nutz.log.Log;
import org.nutz.log.LogAdapter;
import org.nutz.plugin.Plugin;

/**
 * Apache log4j 适配器
 * 
 * <p/>
 * 存在<code>org.apache.log4j.Logger</code>就认为可用.
 * <p/>
 * 同样的,如果存在log4j-over-slf4j,则也会认为可用.
 * <p/>
 * 参考Issue : http://code.google.com/p/nutz/issues/detail?id=322
 * <p/>
 * <b>Log4J 1.2.11及之前的版本不支持Trace级别,默认转为使用Debug级别来Log</b>
 * 
 * @author Young(sunonfire@gmail.com)
 * @author wendal(wendal11985@gmail.com)
 */
public class Log4jLogAdapter implements LogAdapter, Plugin {

    public boolean canWork() {
        try {
            // org.apache.log4j.Logger.class.getName();
            org.apache.logging.log4j.LogManager.class.getName();
            return true;
        }
        catch (Throwable e) {}
        return false;
    }

    public Log getLogger(String className) {
        return new Log4JLogger(className);
    }

    static class Log4JLogger extends AbstractLog {

        public static final String SUPER_FQCN = AbstractLog.class.getName();
        public static final String SELF_FQCN = Log4JLogger.class.getName();

        private Logger logger;

        private static boolean hasTrace;

        static {
            try {
                Level.class.getDeclaredField("TRACE");
                hasTrace = true;
            }
            catch (Throwable e) {}
        }

        Log4JLogger(String className) {
            logger = LogManager.getLogger(className);
            isFatalEnabled = logger.isEnabled(Level.FATAL);
            isErrorEnabled = logger.isEnabled(Level.ERROR);
            isWarnEnabled = logger.isEnabled(Level.WARN);
            isInfoEnabled = logger.isEnabled(Level.INFO);
            isDebugEnabled = logger.isEnabled(Level.DEBUG);
            if (hasTrace)
                isTraceEnabled = logger.isEnabled(Level.TRACE);
        }

        public void debug(Object message, Throwable t) {
            if (isDebugEnabled())
                logger.debug(message.toString(), t);
        }

        public void error(Object message, Throwable t) {
            if (isErrorEnabled())
                logger.error(message.toString(), t);
        }

        public void fatal(Object message, Throwable t) {
            if (isFatalEnabled())
                logger.fatal(message.toString(), t);
        }

        public void info(Object message, Throwable t) {
            if (isInfoEnabled())
                logger.info(message.toString(), t);
        }

        public void trace(Object message, Throwable t) {
            if (isTraceEnabled()) {
                logger.trace(message.toString(), t);
            }
        }

        public void warn(Object message, Throwable t) {
            if (isWarnEnabled())
                logger.warn(message.toString(), t);
        }

        @Override
        protected void doPrintLog(int level, Object message, Throwable tx) {

            switch (level) {
            case LEVEL_FATAL:
                logger.log(Level.FATAL, message, tx);
                break;
            case LEVEL_ERROR:
                logger.log(Level.ERROR, message, tx);
                break;
            case LEVEL_WARN:
                logger.log(Level.WARN, message, tx);
                break;
            case LEVEL_INFO:
                logger.log(Level.INFO, message, tx);
                break;
            case LEVEL_DEBUG:
                logger.log(Level.DEBUG, message, tx);
                break;
            case LEVEL_TRACE:
                logger.log(Level.TRACE, message, tx);
                break;
            default:
                break;
            }
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isEnabled(Level.ERROR);
        }

        @Override
        public boolean isFatalEnabled() {
            return logger.isEnabled(Level.FATAL);
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled();
        }

        @Override
        public boolean isTraceEnabled() {
            if (!hasTrace)
                return logger.isDebugEnabled();
            return logger.isTraceEnabled();
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isEnabled(Level.WARN);
        }
    }
}
