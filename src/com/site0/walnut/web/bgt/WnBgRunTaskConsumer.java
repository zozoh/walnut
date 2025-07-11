package com.site0.walnut.web.bgt;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.nutz.lang.random.R;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.log.Log;
import org.nutz.trans.Atom;

import com.site0.walnut.api.box.WnServiceFactory;
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
import com.site0.walnut.web.WnConfig;

public class WnBgRunTaskConsumer implements Runnable {

    private static final Log log = Wlog.getBG_TASK();

    private WnSysTaskApi taskApi;
    private WnLoginApi auth;
    private WnBoxRunning running;
    private WnSession rootSession;

    // 创建一个固定大小为 3 的线程池
    private ThreadPoolExecutor execPool;

    public WnBgRunTaskConsumer(WnConfig config, WnServiceFactory sf, WnRun _run, WnSession rootSe) {
        this.taskApi = sf.getTaskApi();
        this.auth = sf.getLoginApi();
        this.running = _run.createRunning(true);
        this.rootSession = rootSe;

        int corePoolSize = config.getInt("bg-task-pool-core-size", 5);
        int maximumPoolSize = config.getInt("bg-task-pool-max-size", 100);
        long keepAliveTime = config.getLong("bg-task-pool-keep-alive", 5000);
        TimeUnit unit = TimeUnit.MILLISECONDS;
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        this.execPool = new ThreadPoolExecutor(corePoolSize,
                                               maximumPoolSize,
                                               keepAliveTime,
                                               unit,
                                               workQueue);
    }

    @Override
    public void run() {
        // 准备上下文信息
        Wn.WC().setSession(rootSession);

        // 准备查询对象
        WnSysTaskQuery q = new WnSysTaskQuery();
        q.setLimit(1);

        // 进入主循环
        while (!Thread.interrupted()) {
            try {
                WnSysTask task = taskApi.popTask(q);

                // 线程池满了，休息3秒
                int pool_a_count = execPool.getActiveCount();
                int pool_max_size = execPool.getMaximumPoolSize();
                if (pool_a_count >= pool_max_size) {
                    log.infof("execPool is full: %s==%s, sleep 3s", pool_a_count, pool_max_size);
                    taskApi.waitForMoreTask(3000);
                    continue;
                }

                // 没有更多任务，那么就睡久一点: 1分钟
                if (null == task) {
                    taskApi.waitForMoreTask(60000);
                    continue;
                }
                log.infof("pop:%s", task.toString());

                // 得到任务的用户
                String userName = task.meta.getString("user");
                WnUser user = auth.checkUser(userName);

                // 执行
                InputStream ins = new ByteInputStream(task.input);

                // 看看线程里是不是报退出
                WnSysTaskException[] _atom_error = new WnSysTaskException[1];

                // TODO 这里用线程池执行才好
                // TODO 执行失败，要不要重新加回队列？ 来一个重试次数啥的
                execPool.submit(new Atom() {
                    public void run() {
                        try {
                            taskApi.runTask(running, task.meta, user, ins);
                        }
                        catch (WnSysTaskException e) {
                            _atom_error[0] = e;
                        }
                    }
                });

                // 有异常了，抛一下
                if (null != _atom_error[0]) {
                    throw _atom_error[0];
                }

                // 任务执行完毕后休息1ms，释放一下 CPU
                Wlang.quiteSleep(1);
            }
            catch (WnSysTaskException e) {
                Throwable e2 = e.getCause();
                // 加锁失败，大多数情况是其他节点的 Task线程正在搞事情
                // 这个有点巧，所以睡了几秒再试试，应该就能成功
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
                    log.warn("Run Task Error", e);
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
