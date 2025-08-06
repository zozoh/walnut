package com.site0.walnut.impl.box;

import java.io.InputStream;
import java.io.OutputStream;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.stream.VoidInputStream;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import org.nutz.trans.Atom;
import com.site0.walnut.api.box.WnBoxContext;
import com.site0.walnut.api.box.WnBoxService;
import com.site0.walnut.api.box.WnBoxStatus;
import com.site0.walnut.api.box.WnTunnel;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.hook.WnHookContext;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.api.io.WnSecurity;
import com.site0.walnut.impl.io.WnSecurityImpl;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.JvmTunnel;
import com.site0.walnut.util.SyncWnTunnel;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmplX;

import org.nutz.web.WebException;

public class JvmAtomRunner {

    private static final Log log = Wlog.getBOX();

    WnBoxService boxes;

    String boxId;

    private Object idleLock;

    WnBoxStatus status;

    JvmAtom[] atoms; // 每个命令的原子

    Thread[] threads; // 启动了哪些线程

    WnTunnel[] tnls; // 记录了线程间所有的隧道

    // List<OutputStream> opss; // 有多少重定向输出

    WnBoxContext bc;

    public OutputStream out;

    public OutputStream err;

    public InputStream in;

    JvmExecutorFactory jef;

    public JvmAtomRunner(WnBoxService boxes) {
        this.idleLock = new Object();
        this.boxes = boxes;
    }

    public JvmAtomRunner clone() {
        JvmAtomRunner re = new JvmAtomRunner(boxes);
        re.boxId = boxId;
        re.status = WnBoxStatus.IDLE;
        re.bc = bc;
        re.out = out;
        re.in = in;
        re.err = err;
        re.jef = jef;
        return re;
    }

    private String __extend_substitution(String cmdLine, JvmBoxOutput boxErr) {
        JvmAtomSubstitutionCallback tc = new JvmAtomSubstitutionCallback(this, boxErr);

        // 开始逐次寻找预处理段
        try {
            Ws.splitQuoteToken(cmdLine, "`", null, tc);
        }
        // 一定要记得还原啊还原到老的输出流
        finally {
            this.out = tc.oldOut;
            this.err = tc.oldErr;
        }

        // 返回处理后的字符串
        return tc.sb.toString();
    }

    public void run(String cmdLine) {
        // 忽略空行和注释行
        if (Strings.isBlank(cmdLine) || cmdLine.matches("^[ \t]*(#|//).*$")) {
            return;
        }

        // 准备错误输出
        JvmBoxOutput boxErr = new JvmBoxOutput(err);

        // 首先对命令行进行预处理
        if (cmdLine.indexOf('`') >= 0) {
            cmdLine = __extend_substitution(cmdLine, boxErr);
            if (log.isDebugEnabled()) {
                log.debugf("JvmAtomRunner.run: extended cmdLine=%s", cmdLine);
            }
        }

        // 预处理失败，就不向下执行了
        if (Strings.isBlank(cmdLine))
            return;

        // 执行预处理环境变量
        // cmdLine = Wn.normalizeStr(cmdLine, bc.session.getVars());
        cmdLine = WnTmplX.exec(cmdLine, bc.session.getEnv());

        if (log.isDebugEnabled()) {
            log.debugf("JvmAtomRunner.run: apply_env cmdLine=%s", cmdLine);
        }

        // 执行处理后的命令行（不再处理预处理指令了）
        try {
            __run(cmdLine, boxErr);
        }
        catch (Throwable e) {
            // 如果不是被 InterruptedException， 记录错误
            if (!Wlang.isCauseBy(e, InterruptedException.class)) {
                // 拆包 ...
                Throwable ue = Er.unwrap(e);

                // 如果仅仅显示警告，则日志记录警告信息
                if (log.isWarnEnabled()) {
                    log.warnf("AR_ERROR: %s", ue.toString());
                }

                // 有必要的话，显示错误堆栈
                if (!(ue instanceof WebException)) {
                    log.warn(String.format("AR_ERROR: %s", ue.toString()), ue);
                }

                // 输出到错误输出
                boxErr.println(ue.toString());
                Streams.safeFlush(boxErr);
            }
        }
    }

    void __run(String cmdLine, JvmBoxOutput boxErr) {
        // 准备标准输出输出
        WnSession se = bc.session;

        // 标记状态
        status = WnBoxStatus.RUNNING;

        // 分析命令，看看有多少管道连接
        String[] cmds = Cmds.splitCmdAtoms(cmdLine);

        if (log.isDebugEnabled()) {
            log.debugf("JvmAtomRunner.__run: cmds.lenght=%d", cmds.length);
        }

        // 准备运行的线程原子
        final WnContext wc = Wn.WC();

        // 启动安全检查接口
        final WnSecurity secu = new WnSecurityImpl(bc.io, bc.auth());

        if (log.isDebugEnabled()) {
            log.debugf("JvmAtomRunner.__run: secu=%s", secu);
        }

        // 如果调用线程设置了钩子，那么本执行器所有的线程也都要执行相同的钩子设定
        // 只是需要确保会话和当前用户与 Box 一致
        WnHookContext hc = wc.getHookContext();
        if (null != hc) {
            hc = hc.clone();
        }

        if (log.isDebugEnabled()) {
            log.debugf("JvmAtomRunner.__run: hc=%s", hc);
        }

        // 分析每个执行原子
        JvmAtom a;
        atoms = new JvmAtom[cmds.length];
        // opss = new ArrayList<OutputStream>(atoms.length);
        for (int i = 0; i < cmds.length; i++) {
            if (log.isDebugEnabled()) {
                log.debugf("JvmAtomRunner.__run: cmds[%d]=%s", i, cmds[i]);
            }
            // 生成原子并解析命令字符串
            try {
                a = new JvmAtom(this, cmds[i]);
            }
            catch (WebException e) {
                boxErr.printlnf("%s : %s", e.getKey(), e.getReasonString());
                throw e;
            }

            // 找到执行器
            a.executor = jef.get(a.cmdName);

            // 没有执行器，直接输出错误
            if (null == a.executor) {
                boxErr.printlnf("e.cmd.notfound : %s", a.cmdName);
                return;
            }

            if (log.isDebugEnabled()) {
                log.debugf("JvmAtomRunner.__run: cmds[%d] executor=%s", i, a.executor.getMyName());
            }

            // 填充对应字段
            a.id = i;
            a.sys = new WnSystem(bc.services);
            a.sys.boxId = this.boxId;
            a.sys.pipeId = i;
            a.sys.nextId = i + 1;
            a.sys.cmdOriginal = cmds[i];
            // a.sys.cmdOriginal = cmds[i];
            a.sys.session = se;
            a.sys.err = boxErr;
            a.sys.io = bc.io;
            a.sys.auth = bc.auth();
            a.sys.jef = jef;
            a.secu = secu;
            a.hc = hc;
            a.parentContext = wc;

            if (log.isDebugEnabled()) {
                log.debugf("JvmAtomRunner.__run: cmds[%d] a.id=%s, a.cmdName=%s",
                           i,
                           a.id,
                           a.cmdName);
            }

            // 看看是否重定向输出
            if (null != a.redirectPath) {
                final JvmAtom _a = a;
                wc.hooking(null, new Atom() {
                    public void run() {
                        wc.security(secu, new Atom() {
                            public void run() {
                                String path = Wn.normalizeFullPath(_a.redirectPath, _a.sys);
                                WnObj o = bc.io.createIfNoExists(null, path, WnRace.FILE);

                                int offset = _a.redirectAppend ? -1 : 0;
                                OutputStream ops = bc.io.getOutputStream(o, offset);

                                _a.sys.out = new JvmBoxOutput(ops);
                                // opss.add(ops);
                            }
                        });
                    }
                });
                a.sys.isOutRedirect = true;
            }

            // 计数
            atoms[i] = a;
        }

        // 为第一个原子分配标准输入
        atoms[0].sys.in = new JvmBoxInput(null == in ? new VoidInputStream() : in);

        if (log.isDebugEnabled()) {
            log.debugf("JvmAtomRunner.__run: atoms.len=%d, atoms[0].sys.in=%s",
                       atoms.length,
                       atoms[0].sys.in);
        }

        // 如果没有重定向，为最后一个原子分配标准输出
        int lastIndex = atoms.length - 1;
        a = atoms[lastIndex];
        if (null == a.redirectPath) {
            a.sys.out = new JvmBoxOutput(out);
            // 指明将错误输出流合并到标准输出
            if (a.redirectErrToStd) {
                a.sys.err = a.sys.out;
            }
        }
        a.sys.nextId = -1; // 最后一个原子 nextId 为 -1 表示没有后续管道原子处理它的输出

        if (log.isDebugEnabled()) {
            log.debugf("JvmAtomRunner.__run: atoms[%d].sys.out=%s", lastIndex, a.sys.out);
        }

        // 为所有中间原子分配管道
        if (atoms.length > 1) {
            tnls = new WnTunnel[lastIndex];
            for (int i = 0; i < lastIndex; i++) {
                a = atoms[i];
                // 如果没重定向输出，则上一个的输出等于下一个输入
                if (a.sys.out == null) {
                    WnTunnel tnl = new SyncWnTunnel(new JvmTunnel(8192));
                    tnls[i] = tnl;
                    a.sys.out = new JvmBoxOutput(tnl.asOutputStream());
                    atoms[i + 1].sys.in = new JvmBoxInput(tnl.asInputStream());
                }
                // 否则为下一个原子分配一个空输入
                else {
                    atoms[i + 1].sys.in = new JvmBoxInput(null);
                }
                // 指明将错误输出流合并到标准输出
                if (a.redirectErrToStd) {
                    a.sys.err = a.sys.out;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("JvmAtomRunner.__run: assigned WnTunnel between atoms");
            }
        }

        // 如果仅有一个原子，那么就在本线程执行
        if (atoms.length == 1) {
            if (log.isDebugEnabled()) {
                log.debug("JvmAtomRunner.__run: atoms[0].run()");
            }
            atoms[0].run();
        }
        // 否则，为每个原子创建一个线程赖运行
        else {
            if (log.isDebugEnabled()) {
                log.debugf("JvmAtomRunner.__run: atoms run in %d Threads", atoms.length);
            }
            threads = new Thread[atoms.length];
            for (int i = 0; i < atoms.length; i++) {
                String tName = "box_" + boxId + "@T" + i + ":" + atoms[i].cmdName;
                threads[i] = new Thread(atoms[i], tName);
            }

            if (log.isDebugEnabled()) {
                log.debugf("JvmAtomRunner.__run: t.start in loop...");
            }
            // 依次启动
            for (Thread t : threads) {
                t.start();
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debugf("JvmAtomRunner.__run: quiet");
        }
    }

    /**
     * 每当一个命令运行原子运行结束，就会通过这个方法标志自己任务结束了
     * <p>
     * 这样，如果全部原子都通知了一遍，在<code>wait_for_idle</code>阻塞的调用者线程会得到通知
     * 
     * @param atom
     *            命令运行原子对象
     */
    synchronized void _finish(JvmAtom atom) {
        atoms[atom.id] = null;
        for (JvmAtom a : atoms)
            if (null != a)
                return;
        status = WnBoxStatus.IDLE;
        Wlang.notifyAll(idleLock);
    }

    /**
     * 这是一个阻塞函数，让调用者在自己的线程死等沙箱运行结束
     */
    public void wait_for_idle() {
        // 如果只有一个原子，根本不用等，否则等通知，等原子运行结束
        if (null != atoms && atoms.length > 1) {
            // 直到全部线程退出，才结束循环
            while (!__is_all_stopped()) {
                // 进入同步块
                synchronized (idleLock) {
                    // 再查一遍，还没结束的话就死等！
                    if (!__is_all_stopped()) {
                        if (log.isDebugEnabled())
                            log.debug("box: no stopped yet, sleep");
                        try {
                            idleLock.wait(0);
                        }
                        catch (InterruptedException e) {
                            throw Wlang.wrapThrow(e);
                        }
                    }
                }
            }
        }

        // 刷新一下输出流
        Streams.safeFlush(out);
        Streams.safeFlush(err);

        // 标记状态
        status = WnBoxStatus.IDLE;

    }

    private boolean __is_all_stopped() {
        if (null != threads)
            for (Thread t : threads) {
                try {
                    t.join();
                }
                catch (InterruptedException e) {
                    throw Wlang.wrapThrow(e);
                }
                if (t.isAlive())
                    return false;
            }
        return true;
    }

    public void __free() {
        // 强制停止所有线程
        if (null != threads)
            for (Thread t : threads)
                if (t.isAlive())
                    t.interrupt();

        // 释放所有打开的句柄
        // if (null != opss)
        // for (OutputStream ops : opss) {
        // Streams.safeClose(ops);
        // }

        // 释放其他资源
        if (log.isDebugEnabled())
            log.debug("box: release resources");

        Streams.safeFlush(out);
        Streams.safeFlush(err);
        Streams.safeClose(out);
        Streams.safeClose(in);
        Streams.safeClose(err);

        if (null != tnls)
            for (WnTunnel tnl : tnls) {
                Streams.safeClose(tnl);
            }

        // 全部退出了，标记状态
        if (log.isDebugEnabled())
            log.debug("box: mark free");

        // 标记状态
        status = WnBoxStatus.FREE;
    }

}
