package org.nutz.walnut.impl.box;

import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.stream.VoidInputStream;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxService;
import org.nutz.walnut.api.box.WnBoxStatus;
import org.nutz.walnut.api.box.WnTunnel;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.hook.WnHookContext;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.impl.io.WnSecurityImpl;
import org.nutz.walnut.util.JvmTunnel;
import org.nutz.walnut.util.SyncWnTunnel;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.Ws;
import org.nutz.web.WebException;

public class JvmAtomRunner {

    private static final Log log = Logs.get();

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
        if (cmdLine.indexOf('`') >= 0)
            cmdLine = __extend_substitution(cmdLine, boxErr);

        // 预处理失败，就不向下执行了
        if (Strings.isBlank(cmdLine))
            return;

        // 执行预处理
        cmdLine = Wn.normalizeStr(cmdLine, bc.session.getVars());

        // 执行处理后的命令行（不再处理预处理指令了）
        try {
            __run(cmdLine, boxErr);
        }
        catch (Throwable e) {
            // 如果不是被 InterruptedException， 记录错误
            if (!Lang.isCauseBy(e, InterruptedException.class)) {
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
        WnAuthSession se = bc.session;

        // 标记状态
        status = WnBoxStatus.RUNNING;

        // 分析命令，看看有多少管道连接
        String[] cmds = Strings.split(cmdLine, true, '|');

        // 准备运行的线程原子
        final WnContext wc = Wn.WC();

        // 启动安全检查接口
        final WnSecurity secu = new WnSecurityImpl(bc.io, bc.auth);

        // 如果调用线程设置了钩子，那么本执行器所有的线程也都要执行相同的钩子设定
        // 只是需要确保会话和当前用户与 Box 一致
        WnHookContext hc = wc.getHookContext();
        if (null != hc) {
            hc = hc.clone();
        }

        // 分析每个执行原子
        JvmAtom a;
        atoms = new JvmAtom[cmds.length];
        // opss = new ArrayList<OutputStream>(atoms.length);
        for (int i = 0; i < cmds.length; i++) {
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
            a.sys.auth = bc.auth;
            a.sys.jef = jef;
            a.secu = secu;
            a.hc = hc;
            a.parentContext = wc;

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

        // 如果没有重定向，为最后一个原子分配标准输出
        int lastIndex = atoms.length - 1;
        a = atoms[lastIndex];
        if (null == a.redirectPath) {
            a.sys.out = new JvmBoxOutput(out);
        }
        a.sys.nextId = -1; // 最后一个原子 nextId 为 -1 表示没有后续管道原子处理它的输出

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
            }
        }

        // 如果仅有一个原子，那么就在本线程执行
        if (atoms.length == 1) {
            atoms[0].run();
        }
        // 否则，为每个原子创建一个线程赖运行
        else {
            threads = new Thread[atoms.length];
            for (int i = 0; i < atoms.length; i++) {
                String tName = "box_" + boxId + "@T" + i + ":" + atoms[i].cmdName;
                threads[i] = new Thread(atoms[i], tName);
            }
            // 依次启动
            for (Thread t : threads) {
                t.start();
            }
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
        Lang.notifyAll(idleLock);
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
                            throw Lang.wrapThrow(e);
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
                    throw Lang.wrapThrow(e);
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
