package org.nutz.walnut.impl.box;

import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.web.WebException;

class JvmAtom extends JvmCmd implements Atom {

    private static final Log log = Logs.get();

    int id;

    WnSystem sys;

    JvmExecutor executor;

    public JvmAtom() {
        sys = new WnSystem();
    }

    @Override
    public void run() {
        try {
            executor.exec(sys, args);
        }
        catch (Throwable e) {
            WebException we = Er.wrap(e);
            // 如果仅仅显示警告，则日志记录警告信息
            if (log.isWarnEnabled()) {
                log.warn(e.toString());
            }
            // 否则如果需要显示更详细警告信息，则打印错误堆栈
            else if (log.isDebugEnabled()) {
                log.warn(e.toString(), e);
            }
            // 输出到错误输出
            sys.err.writeLine(we.toString());
            Streams.safeFlush(sys.err);
        }
        finally {
            Streams.safeFlush(sys.out);
            Streams.safeClose(sys.out);
        }
    }

}
