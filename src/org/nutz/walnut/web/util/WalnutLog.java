package org.nutz.walnut.web.util;

import org.nutz.log.impl.AbstractLog;
import org.nutz.walnut.impl.box.WnSystem;

public class WalnutLog extends AbstractLog {
	
	protected WnSystem sys;
	
	public WalnutLog(WnSystem sys) {
		this.sys = sys;
	}
	
	public WalnutLog(WnSystem sys, int level) {
		this.sys = sys;
		setLevel(level);
	}

	public void fatal(Object message, Throwable t) {
		if (!isFatalEnabled())
			return;
		sys.err.println("fatal:"+message);
	}

	public void error(Object message, Throwable t) {
		if (!isErrorEnabled())
			return;
		sys.err.println("error:"+message);
	}

	public void warn(Object message, Throwable t) {
		if (!isWarnEnabled())
			return;
		sys.out.println("warn :"+message);
	}

	public void info(Object message, Throwable t) {
		if (!isInfoEnabled())
			return;
		sys.out.println("info :"+message);
	}

	public void debug(Object message, Throwable t) {
		if (!isDebugEnabled())
			return;
		sys.out.println("debug:"+message);
	}

	public void trace(Object message, Throwable t) {
		if (!isWarnEnabled())
			return;
		sys.out.println("trace:"+message);
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
		this.isWarnEnabled =  LEVEL_WARN  >= level;
		this.isInfoEnabled =  LEVEL_INFO  >= level;
		this.isDebugEnabled = LEVEL_DEBUG >= level;
		this.isTraceEnabled = LEVEL_TRACE >= level;
	}
}
