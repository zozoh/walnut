package org.nutz.walnut.impl.box;

import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.jetty.util.log.Log;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.random.R;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.box.WnBoxRuntime;
import org.nutz.walnut.api.box.WnBoxStatus;
import org.nutz.walnut.api.box.WnTurnnel;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.JvmTurnnel;

public class JvmBox implements WnBox {

    private Object idleLock;

    WnBoxContext bc;

    OutputStream out;

    OutputStream err;

    InputStream in;

    WnBoxStatus status;

    WnBoxRuntime runtime;

    JvmExecutorFactory executors;

    private String id;

    private JvmAtom[] atoms;

    private Thread[] threads;

    private WnTurnnel[] tnls;

    public JvmBox() {
        id = R.UU32();
        runtime = new WnBoxRuntime();
        idleLock = new Object();
        status = WnBoxStatus.IDLE;
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
    public void run(String cmdLines) {
        // 标记状态
        status = WnBoxStatus.RUNNING;

        // 分析命令，看看有多少管道连接
        String[] cmds = null;

        // TODO ...

        // 准备标准输出输出
        JvmBoxOutput boxErr = new JvmBoxOutput(err);
        WnSession se = bc.session.clone();

        // 准备运行的线程原子
        atoms = new JvmAtom[cmds.length];
        for (int i = 0; i < cmds.length; i++) {
            // 生成原子
            JvmAtom a = new JvmAtom();
            String cmd = cmds[i];
            Jvms.parseCommand(a, cmd);

            // 找到执行器
            a.executor = executors.check(a.cmdName);

            // 填充对应字段
            a.id = i;
            a.sys.se = se;
            // a.sys.err = boxErr;

            // 计数
            i++;
        }

        // 为第一个原子分配标准输入
        atoms[0].sys.in = new JvmBoxInput(in);

        // 为最后一个原子分配标准输出
        int lastIndex = atoms.length - 1;
        atoms[lastIndex].sys.out = new JvmBoxOutput(out);

        // 为所有中间原子分配管道
        if (atoms.length > 1) {
            tnls = new WnTurnnel[lastIndex];
            for (int i = 0; i < lastIndex; i++) {
                JvmTurnnel tnl = new JvmTurnnel(8192);
                tnls[i] = tnl;
                atoms[i].sys.out = new JvmBoxOutput(tnl.asOutputStream());
                atoms[i + 1].sys.in = new JvmBoxInput(tnl.asInputStream());
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

    synchronized void finish(JvmAtom atom) {
        atoms[atom.id] = null;
        for (JvmAtom a : atoms)
            if (null != a)
                return;
        status = WnBoxStatus.IDLE;
        Lang.notifyAll(idleLock);
    }

    @Override
    public void waitForIdle() {
        // 如果只有一个原子，根本不用等，否则等通知，等原子运行结束
        if (atoms.length >= 1) {
            Lang.wait(idleLock, 0);

            if (Log.isDebugEnabled())
                Log.debug("box: check all stopped");

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
                if (Log.isDebugEnabled())
                    Log.debug("box: no stopped yet, sleep 5ms");
                Lang.wait(idleLock, 5);
            }
        }

        // 释放资源
        if (Log.isDebugEnabled())
            Log.debug("box: release resources");
        Streams.safeClose(out);
        Streams.safeClose(in);
        Streams.safeClose(err);

        if (null != tnls)
            for (WnTurnnel tnl : tnls)
                Streams.safeClose(tnl);

        // 全部退出了，标记状态
        if (Log.isDebugEnabled())
            Log.debug("box: mark idle");
        status = WnBoxStatus.IDLE;
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
