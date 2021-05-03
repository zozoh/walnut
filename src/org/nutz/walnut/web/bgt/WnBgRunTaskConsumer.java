package org.nutz.walnut.web.bgt;

import java.io.InputStream;

import org.nutz.lang.random.R;
import org.nutz.lang.util.ByteInputStream;
import org.nutz.log.Log;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.box.WnServiceFactory;
import org.nutz.walnut.api.lock.WnLockBusyException;
import org.nutz.walnut.api.lock.WnLockFailException;
import org.nutz.walnut.ext.sys.task.WnSysTask;
import org.nutz.walnut.ext.sys.task.WnSysTaskApi;
import org.nutz.walnut.ext.sys.task.WnSysTaskException;
import org.nutz.walnut.ext.sys.task.WnSysTaskQuery;
import org.nutz.walnut.impl.srv.WnBoxRunning;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.util.WnRun;

public class WnBgRunTaskConsumer implements Runnable {

    private static final Log log = Wlog.getBG_TASK();

    WnSysTaskApi taskApi;
    WnAuthService auth;
    WnBoxRunning running;

    public WnBgRunTaskConsumer(WnServiceFactory sf, WnRun _run) {
        this.taskApi = sf.getTaskApi();
        this.auth = sf.getAuthApi();
        this.running = _run.createRunning(true);
    }

    @Override
    public void run() {
        // 准备查询对象
        WnSysTaskQuery q = new WnSysTaskQuery();
        q.setLimit(1);

        // 进入主循环
        while (!Thread.interrupted()) {
            try {
                WnSysTask task = taskApi.popTask(q);

                // 没有更多任务，那么就睡久一点: 1分钟
                if (null == task) {
                    Wlang.wait(taskApi, 60000);
                    continue;
                }
                log.info("pop one");

                // 得到任务的用户
                String userName = task.meta.getString("user");
                WnAccount user = auth.checkAccount(userName);

                // 执行
                InputStream ins = new ByteInputStream(task.input);
                taskApi.runTask(running, task.meta, user, ins);

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
