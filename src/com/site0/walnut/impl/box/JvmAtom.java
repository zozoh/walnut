package com.site0.walnut.impl.box;

import java.util.TimeZone;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import org.nutz.log.Log;
import org.nutz.trans.Atom;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.hook.WnHookContext;
import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;

class JvmAtom extends JvmCmd implements Atom {

    private static final Log log = Wlog.getBOX();

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
                // 对于 @xxx 的过滤型命令，还是需要保留引号的，因为有可能值就是 '@xxx'
                String[] args2 = executor.prepareArgs(args);
                executor.exec(sys, args2);
            }
            catch (Exception e) {
                // 如果是 Eof 就忍了
                Throwable e2 = Wlang.unwrapThrow(e);
                if (e2 instanceof org.eclipse.jetty.io.EofException) {
                    if (log.isDebugEnabled())
                        log.debug("EofException cached");
                }
                // 否则抛出
                else {
                    throw Er.wrap(e);
                }
            }
        }
        catch (Throwable e) {
            // 如果不是被 InterruptedException， 记录错误
            if (!Wlang.isCauseBy(e, InterruptedException.class)) {
                // 拆包 ...
                Throwable ue = Er.unwrap(e);

                String errMsg = Ws.sBlanks(ue.toString(), ue.getMessage(), ue.getClass().getName());

                // 将错误输出到标准输出
                if (this.redirectErrToStd) {
                    // 这样 ajaxre 可以自动判断异常字符串
                    if (!errMsg.startsWith("e.")) {
                        errMsg = "e." + errMsg;
                    }
                    sys.out.println(errMsg);
                    Streams.safeFlush(sys.out);
                }
                // 输出到错误输出
                else {
                    // 有必要的话，显示错误堆栈
                    if (log.isWarnEnabled()) {
                        log.warnf("Atom[%d] ERROR: %s : %s", id, errMsg, ue);
                    }
                    sys.err.println(errMsg);
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
        wc.setSession(sys.session);

        // 如果不是父线程运行的，则 copy 父线程变量
        if (parentContext != wc) {
            wc.putAll(parentContext);
        }

        // 从会话中获得时区
        TimeZone tZone = Wtime.getSessionTimeZone(sys.session);
        if (null != tZone) {
            wc.setTimeZone(tZone);
        }

        // 设置钩子，安全接口，等，然后运行
        wc.hooking(hc, new Atom() {
            public void run() {
                wc.security(secu, new Atom() {
                    public void run() {
                        wc.su(sys.getMe(), new Atom() {
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
