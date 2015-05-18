package org.nutz.walnut.impl.box;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Wn;

class JvmAtom extends JvmCmd implements Atom {

    private static final Log log = Logs.get();

    int id;

    WnSystem sys;

    JvmExecutor executor;

    JvmAtomRunner runner;

    public JvmAtom(JvmAtomRunner box, String src) {
        super(src);
        this.runner = box;
    }

    @Override
    public void run() {
        try {
            Wn.WC().SE(sys.se);
            sys._runner = runner.clone();
            if (log.isDebugEnabled())
                log.debugf("Atom(%s) before : %s", id, sys.original);
            executor.exec(sys, args);
        }
        catch (Throwable e) {
            // 如果不是被 InterruptedException， 记录错误
            if (!Lang.isCauseBy(e, InterruptedException.class)) {
                // 拆包 ...
                Throwable ue = Er.unwrap(e);

                // 否则如果需要显示更详细警告信息，则打印错误堆栈
                if (log.isDebugEnabled()) {
                    log.warn(String.format("Atom[%d] ERROR: %s", id, e.toString()), e);
                }
                // 如果仅仅显示警告，则日志记录警告信息
                else if (log.isWarnEnabled()) {
                    log.warnf("Atom[%d] ERROR: %s", id, e.toString());
                }
                // 输出到错误输出
                sys.err.println(ue.toString());
                Streams.safeFlush(sys.err);
            }
        }
        finally {
            if (log.isDebugEnabled())
                log.debugf("Atom[%s] DONE", id);

            Streams.safeFlush(sys.out);
            // 不是最后一个 Atom则不是整个 box 的输出，因此需要关闭
            if (sys.nextId > 0) {
                Streams.safeClose(sys.out);
            }
            runner._finish(this);
        }
    }

}
