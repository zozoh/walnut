package com.site0.walnut.web2.bgt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.random.R;
import org.nutz.lang.random.StringGenerator;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.log.Log;

import com.site0.walnut.api.lock.WnLockBusyException;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.ext.sys.task.WnSysTask;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.ext.sys.task.WnSysTaskException;
import com.site0.walnut.ext.sys.task.WnSysTaskQuery;
import com.site0.walnut.impl.srv.WnBoxRunning;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnRun;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 系统后台任务消费线程（对应旧 {@code WnBgRunTaskConsumer}）。
 * <p>
 * 与旧实现的差别：线程自己持有 {@link WnServiceFactory} 引用取服务，不依赖 Nutz；
 * 提供可中断的 {@link #shutdown()}。
 */
public class WnBgTaskThread extends Thread {

    private static final Log log = Wlog.getBG_TASK();

    private static final StringGenerator sg = R.sg(8);

    private final WnServiceFactory factory;

    private final WnSysTaskApi taskApi;

    private final WnLoginApi auth;

    private final WnRun run;

    private final WnSession rootSession;

    public WnBgTaskThread(WnServiceFactory factory, WnRun run, WnSession rootSession) {
        super("BGT_TASK");
        this.factory = factory;
        this.taskApi = factory.getServiceHolder().getTaskApi();
        this.auth = factory.getServiceHolder().getLoginApi();
        this.run = run;
        this.rootSession = rootSession;
        this.setDaemon(true);
    }

    /**
     * 优雅停止：interrupt + join(5s)。可重复调用。
     */
    public void shutdown() {
        if (!this.isAlive()) {
            return;
        }
        this.interrupt();
        try {
            this.join(5000);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (this.isAlive()) {
            log.warn("BGT_TASK still alive after 5s, give up waiting (daemon)");
        }
    }

    @Override
    public void run() {
        // 准备上下文信息
        Wn.WC().setSession(rootSession);

        // 一次最多消费 1000 个任务
        WnSysTaskQuery q = new WnSysTaskQuery();
        q.setLimit(1000);

        // 进入主循环
        while (!Thread.interrupted()) {
            try {
                // 确保会话是激活的
                auth.touchSession(rootSession);

                // 全部弹出
                List<WnSysTask> tasks = taskApi.popAllTasks(q);
                int N = tasks.size();

                // 没有更多任务，那么就睡久一点: 1 分钟
                if (null == tasks || tasks.isEmpty()) {
                    taskApi.waitForMoreTask(60000);
                    continue;
                }
                log.infof("pop: task=%s", N);

                // 准备一个 Thread 列表，一起执行
                List<Thread> rtList = new ArrayList<>(tasks.size());
                WnSysTaskException[] errors = new WnSysTaskException[1];

                // 准备线程
                for (int i = 0; i < tasks.size(); i++) {
                    WnSysTask task = tasks.get(i);
                    // 得到任务的用户
                    String userName = task.meta.getString("user");
                    WnUser user = auth.checkUser(userName);
                    String userId = null;
                    if (null != user) {
                        userId = user.getId();
                    }

                    if (log.isInfoEnabled()) {
                        log.infof("task-%d: uid=%s,unm=%s : %s",
                                  i,
                                  userId,
                                  userName,
                                  task.toString());
                    }

                    // 准备执行
                    InputStream ins = new ByteInputStream(task.input);
                    String threadName = "Task-Runer-" + i + "-" + sg.next();
                    int threadI = i;
                    // TODO 这里需要采用平台虚拟线程
                    // TODO 执行失败，要不要重新加回队列？ 来一个重试次数啥的
                    Thread rt = new Thread(new Runnable() {
                        public void run() {
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debugf("_run.createRuning: user(%s)=%s, cmd=%s",
                                               user.getId(),
                                               user.getName(),
                                               task.meta.getString("command"));
                                }

                                WnBoxRunning running = run.createRunning(true);

                                if (log.isDebugEnabled()) {
                                    log.debugf("runTask: user(%s)=%s, cmd=%s",
                                               user.getId(),
                                               user.getName(),
                                               task.meta.getString("command"));
                                }

                                taskApi.runTask(running, task.meta, user, ins);
                            }
                            catch (WnSysTaskException e) {
                                errors[threadI] = e;
                            }
                        }
                    }, threadName);
                    // 守护线程：极端情况下（任务内部吞掉中断）也不阻塞 JVM 退出
                    rt.setDaemon(true);
                    rtList.add(rt);
                }

                // 启动线程
                log.infof("start thread x %d", N);
                for (Thread t : rtList) {
                    t.start();
                }

                // 等待线程执行完毕（每个最多等 3 秒；停机中断会立刻打断 join）
                log.infof("join thread x %d", N);
                for (Thread t : rtList) {
                    t.join(3000);
                }
                // 还没跑完的补发中断
                for (Thread t : rtList) {
                    if (t.isAlive()) {
                        log.infof("interrupt unfinished task thread: %s", t.getName());
                        t.interrupt();
                    }
                }

                // 处理线程的错误
                int errCount = 0;
                for (int i = 0; i < errors.length; i++) {
                    WnSysTaskException e = errors[i];
                    if (null != e) {
                        errCount++;
                        String msg = String.format("Run Task[%d] Error", i);
                        log.warn(msg, e);
                    }
                }
                log.infof("errCount=%d", errCount);

                // 任务执行完毕后休息 1ms，释放一下 CPU
                Wlang.quiteSleep(1);
            }
            catch (WnSysTaskException e) {
                Throwable e2 = e.getCause();
                // 加锁失败，大多数情况是其他节点的 Task 线程正在搞事情
                if (e2 instanceof WnLockFailException) {
                    long ms = R.random(300, 10000);
                    log.infof("Fail to tryLoack, sleep %dms to retry", ms);
                    Wlang.quiteSleep(ms);
                }
                // 忙锁：大家都很抢啊，多等一会儿
                else if (e2 instanceof WnLockBusyException) {
                    long ms = R.random(1000, 5000);
                    log.infof("Fail to askLoack, sleep %dms to retry", ms);
                    Wlang.quiteSleep(ms);
                }
                // 就是一个退出指令
                else if (e2 != null && e2 instanceof InterruptedException) {
                    break;
                }
                // 其他错误就打印一下
                else {
                    log.warn("Other WnSysTaskException", e);
                    Wlang.quiteSleep(1);
                }
            }
            catch (Throwable e) {
                Throwable e2 = e.getCause();
                // 就是一个退出指令
                if (e2 != null && e2 instanceof InterruptedException) {
                    break;
                }
                log.warn("Other Error", e);
                Wlang.quiteSleep(1);
            }
        }
        // 退出日志
        log.info("quit safely");
    }

}
