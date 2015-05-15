package org.nutz.walnut.impl.box;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.random.R;
import org.nutz.lang.stream.NullInputStream;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxRuntime;
import org.nutz.walnut.api.box.WnBoxStatus;
import org.nutz.walnut.api.box.WnTunnel;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.util.JvmTunnel;
import org.nutz.walnut.util.SyncWnTunnel;
import org.nutz.walnut.util.Wn;

public class JvmBox implements WnBox {

    private static final Log log = Logs.get();

    private Object idleLock;

    WnBoxContext bc;

    OutputStream out;

    OutputStream err;

    InputStream in;

    WnBoxStatus status;

    WnBoxRuntime runtime;

    JvmExecutorFactory jef;

    private String id;

    private JvmAtom[] atoms; // 每个命令的原子

    private Thread[] threads; // 启动了哪些线程

    private WnTunnel[] tnls; // 记录了线程间所有的隧道

    private List<OutputStream> opss; // 有多少重定向输出

    private LinkedBlockingQueue<String> cmdQueue;

    public JvmBox() {
        id = R.UU32();
        runtime = new WnBoxRuntime();
        idleLock = new Object();
        status = WnBoxStatus.IDLE;
        cmdQueue = new LinkedBlockingQueue<String>();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public WnBoxStatus status() {
        return status;
    }

    @Override
    public WnBoxRuntime runtime() {
        return runtime;
    }

    @Override
    public void setup(WnBoxContext bc) {
        this.bc = bc;
    }

    @Override
    public void submit(String cmdLine) {
        if (!cmdQueue.add(cmdLine))
            throw Lang.impossible();
    }

    @Override
    public void run() {
        while (!cmdQueue.isEmpty()) {
            String cmdLine = cmdQueue.poll();
            this.__run(cmdLine);
            this.__wait_for_idle();
        }
    }

    @SuppressWarnings("resource")
    private void __run(String cmdLine) {
        // 准备标准输出输出
        JvmBoxOutput boxErr = new JvmBoxOutput(err);
        WnSession se = bc.session.clone();
        WnUsr me = bc.me.clone();

        // 标记状态
        status = WnBoxStatus.RUNNING;

        // 分析命令，看看有多少管道连接
        String[] cmds = Jvms.split(cmdLine, true, '|');

        // 准备运行的线程原子
        JvmAtom a;
        atoms = new JvmAtom[cmds.length];
        opss = new ArrayList<OutputStream>(atoms.length);
        for (int i = 0; i < cmds.length; i++) {
            // 生成原子并解析命令字符串
            a = new JvmAtom(this, cmds[i]);

            // 找到执行器
            a.executor = jef.get(a.cmdName);

            // 没有执行器，直接输出错误
            if (null == a.executor) {
                boxErr.printlnf("e.cmd.notfound : %s", a.cmdName);
                return;
            }

            // 填充对应字段
            a.id = i;
            a.sys = new WnSystem();
            a.sys.pipeId = i;
            a.sys.nextId = i + 1;
            a.sys.original = cmds[i];
            a.sys.original = cmds[i];
            a.sys.se = se;
            a.sys.me = me;
            a.sys.err = boxErr;
            a.sys.io = bc.io;
            a.sys.usrService = bc.usrService;
            a.sys.sessionService = bc.sessionService;
            a.sys.jef = jef;
            a.sys.box = this;

            // 看看是否重定向输出
            if (null != a.redirectPath) {
                String path = Wn.normalizeFullPath(a.redirectPath, a.sys);
                WnObj o = bc.io.createIfNoExists(null, path, WnRace.FILE);
                OutputStream ops = bc.io.getOutputStream(o, a.redirectAppend ? -1 : 0);
                a.sys.out = new JvmBoxOutput(ops);
                opss.add(ops);
            }

            // 计数
            atoms[i] = a;
        }

        // 为第一个原子分配标准输入
        atoms[0].sys.in = new JvmBoxInput(null == in ? new NullInputStream() : in);

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
                String tName = "box_" + id + "@T" + i + ":" + atoms[i].cmdName;
                threads[i] = new Thread(atoms[i], tName);
            }
            // 依次启动
            for (Thread t : threads) {
                t.start();
            }
        }
    }

    synchronized void _finish(JvmAtom atom) {
        atoms[atom.id] = null;
        for (JvmAtom a : atoms)
            if (null != a)
                return;
        status = WnBoxStatus.IDLE;
        Lang.notifyAll(idleLock);
    }

    private void __wait_for_idle() {
        // 如果只有一个原子，根本不用等，否则等通知，等原子运行结束
        if (null != atoms && atoms.length > 1) {
            if (!__is_all_stopped()) {
                Lang.wait(idleLock, 0);
            }

            if (log.isDebugEnabled())
                log.debug("box: check all stopped");

            // 如果收到了通知，那么等1毫秒，然后频繁检查
            // 睡1ms，会导致后面的线程都被执行一遍再执行当前线程
            try {
                Thread.sleep(1);
            }
            catch (InterruptedException e) {
                throw Lang.wrapThrow(e);
            }

            // 直到全部线程退出，才结束循环
            while (!__is_all_stopped()) {
                if (log.isDebugEnabled())
                    log.debug("box: no stopped yet, sleep 5ms");
                Lang.wait(idleLock, 5);
            }
        }

        // 标记状态
        status = WnBoxStatus.IDLE;

    }

    void free() {
        // 强制停止所有线程
        if (null != threads)
            for (Thread t : threads)
                if (t.isAlive())
                    t.interrupt();

        // 释放所有打开的句柄
        if (null != opss)
            for (OutputStream ops : opss) {
                Streams.safeClose(ops);
            }

        // 释放其他资源
        if (log.isDebugEnabled())
            log.debug("box: release resources");
        Streams.safeClose(out);
        Streams.safeClose(in);
        Streams.safeClose(err);

        if (null != tnls)
            for (WnTunnel tnl : tnls)
                Streams.safeClose(tnl);

        // 全部退出了，标记状态
        if (log.isDebugEnabled())
            log.debug("box: mark idle");
    }

    private boolean __is_all_stopped() {
        for (Thread t : threads)
            if (t.isAlive())
                return false;
        return true;
    }

    @Override
    public void setStdout(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setStderr(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setStdin(InputStream in) {
        this.in = in;
    }

}
