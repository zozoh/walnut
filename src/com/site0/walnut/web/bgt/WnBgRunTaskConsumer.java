package com.site0.walnut.web.bgt;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.nutz.lang.random.R;
import org.nutz.lang.random.StringGenerator;
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
    private static StringGenerator sg = R.sg(8);

    private WnSysTaskApi taskApi;
    private WnLoginApi auth;
    private WnRun _run;
    // private WnBoxRunning running;
    private WnSession rootSession;

    // // 创建一个固定大小为 3 的线程池
    // private ThreadPoolExecutor execPool;

    public WnBgRunTaskConsumer(WnConfig config,
                               WnServiceFactory sf,
                               WnRun _run,
                               WnSession rootSe) {
        this.taskApi = sf.getTaskApi();
        this.auth = sf.getLoginApi();
        this._run = _run;
        // this.running = _run.createRunning(true);
        this.rootSession = rootSe;

        // int corePoolSize = config.getInt("bg-task-pool-core-size", 5);
        // int maximumPoolSize = config.getInt("bg-task-pool-max-size", 100);
        // long keepAliveTime = config.getLong("bg-task-pool-keep-alive", 5000);
        // TimeUnit unit = TimeUnit.MILLISECONDS;
        // BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        // this.execPool = new ThreadPoolExecutor(corePoolSize,
        // maximumPoolSize,
        // keepAliveTime,
        // unit,
        // workQueue);
    }

    @Override
    public void run() {
        // 准备上下文信息
        Wn.WC().setSession(rootSession);

        // 一次最多消费1000个任务
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

                // 没有更多任务，那么就睡久一点: 1分钟
                if (null == tasks || tasks.isEmpty()) {
                    taskApi.waitForMoreTask(60000);
                    continue;
                }
                log.infof("pop: task=%s", N);

                // 准备一个 Thread 列表, 一起执行
                List<Thread> _rt_list = new ArrayList<>(tasks.size());
                WnSysTaskException[] _errors = new WnSysTaskException[1];

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
                    Thread _rt = new Thread(new Atom() {
                        public void run() {
                            try {
                                if (log.isDebugEnabled()) {
                                    log.debugf("_run.createRuning: user(%s)=%s, cmd=%s",
                                               user.getId(),
                                               user.getName(),
                                               task.meta.getString("command"));
                                }

                                WnBoxRunning running = _run.createRunning(true);

                                if (log.isDebugEnabled()) {
                                    log.debugf("runTask: user(%s)=%s, cmd=%s",
                                               user.getId(),
                                               user.getName(),
                                               task.meta.getString("command"));
                                }

                                taskApi.runTask(running, task.meta, user, ins);
                            }
                            catch (WnSysTaskException e) {
                                _errors[threadI] = e;
                            }
                        }
                    }, threadName);
                    _rt_list.add(_rt);
                }

                // 启动线程
                log.infof("start thread x %d", N);
                for (Thread t : _rt_list) {
                    t.start();
                }

                // 等待线程执行完毕
                log.infof("join thread x %d", N);
                for (Thread t : _rt_list) {
                    t.join();
                }

                // 处理线程的错误
                int errCount = 0;
                for (int i = 0; i < _errors.length; i++) {
                    WnSysTaskException e = _errors[i];
                    if (null != e) {
                        errCount++;
                        String msg = String.format("Run Task[%d] Error", i);
                        log.warn(msg, e);
                    }
                }
                log.infof("errCount=%d", errCount);

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
