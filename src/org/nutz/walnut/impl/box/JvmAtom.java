package org.nutz.walnut.impl.box;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;

class JvmAtom extends JvmCmd implements Atom {

    private static final Log log = Logs.get();

    int id;

    WnSystem sys;

    JvmExecutor executor;

    JvmBox box;

    public JvmAtom(JvmBox box, String src) {
        super(src);
        this.box = box;
    }

    @Override
    public void run() {
        try {
            executor.exec(sys, args);
        }
        catch (Throwable e) {
            // 如果不是被 InterruptedException， 记录错误
            if (Lang.isCauseBy(e, InterruptedException.class)) {
                // 拆包 ...
                Throwable ue = Er.unwrap(e);

                // 如果仅仅显示警告，则日志记录警告信息
                if (log.isWarnEnabled()) {
                    log.warn(e.toString());
                }
                // 否则如果需要显示更详细警告信息，则打印错误堆栈
                else if (log.isDebugEnabled()) {
                    log.warn(e.toString(), e);
                }
                // 输出到错误输出
                sys.err.writeLine(ue.toString());
                Streams.safeFlush(sys.err);
            }
        }
        finally {
            Streams.safeFlush(sys.out);
            Streams.safeClose(sys.out);
            box._finish(this);
        }
    }

}
