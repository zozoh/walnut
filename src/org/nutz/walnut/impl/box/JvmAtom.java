package org.nutz.walnut.impl.box;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.web.WebException;

class JvmAtom extends JvmCmd implements Atom {

    private static final Log log = Logs.get();

    int id;

    WnSystem sys;

    WnSecurity secu;

    WnHookContext hc;

    WnContext parentContext; // 记录，以便 copy 父线程的一些临时变量

    JvmExecutor executor;

    JvmAtomRunner runner;

    public JvmAtom(JvmAtomRunner runner, String src) {
        super(src);
        this.runner = runner;
    }

    private void __run() {
        try {
            try {
                executor.exec(sys, args);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        catch (Throwable e) {
            // 如果不是被 InterruptedException， 记录错误
            if (!Lang.isCauseBy(e, InterruptedException.class)) {
                // 拆包 ...
                Throwable ue = Er.unwrap(e);

                // 如果仅仅显示警告，则日志记录警告信息
                if (log.isWarnEnabled()) {
                    log.warnf("Atom[%d] ERROR: %s", id, ue.toString());
                }

                // 有必要的话，显示错误堆栈
                if (!(ue instanceof WebException)) {
                    log.warn(String.format("Atom[%d] ERROR: %s", id, ue.toString()), ue);
                }

                // 将错误输出到标准输出
                if (this.redirectErrToStd) {
                    sys.out.println(ue.toString());
                    Streams.safeFlush(sys.out);
                }
                // 输出到错误输出
                else {
                    sys.err.println(ue.toString());
                    Streams.safeFlush(sys.err);
                }
            }
        }
        finally {
            if (log.isDebugEnabled())
                log.debugf("Atom[%s] DONE", id);

            Streams.safeFlush(sys.out);
            // 不是最后一个 Atom则不是整个 box 的输出，因此需要关闭
            if (sys.nextId > 0 || null != this.redirectPath) {
                Streams.safeClose(sys.out);
            }
            runner._finish(this);
        }
    }

    @Override
    public void run() {
        // 设置会话
        sys._runner = runner.clone();

        if (log.isDebugEnabled())
            log.debugf("Atom(%s) before : %s", id, sys.cmdOriginal);

        // 切换线程上下文到当前用户，并执行业务逻辑
        final WnContext wc = Wn.WC();
        wc.SE(sys.se);

        // 如果不是父线程运行的，则 copy 父线程变量
        if (parentContext != wc) {
            wc.putAll(parentContext);
        }

        // 设置钩子，安全接口，等，然后运行
        wc.hooking(hc, new Atom() {
            public void run() {
                wc.security(secu, new Atom() {
                    public void run() {
                        wc.su(sys.me, new Atom() {
                            public void run() {
                                __run();
                            }
                        });
                    }
                });
            }
        });

    }

}
