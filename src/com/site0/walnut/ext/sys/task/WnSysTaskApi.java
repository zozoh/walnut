package com.site0.walnut.ext.sys.task;

import java.io.InputStream;
import java.util.List;

import com.site0.walnut.api.WnAuthExecutable;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.usr.WnUser;

public interface WnSysTaskApi {

    /**
     * 通知等待的消费线程，有新任务来了
     */
    void notifyForNewTaskComing();

    /**
     * 消费线程通过这个方法可以挂起等待
     * 
     * @param waitInMs
     *            挂起等待时长，如果小于等于 0 则根据子类自己理解 譬如 0 可以认为是永久等待， 或者小于等于 0
     *            可以给一个默认值，譬如1分钟
     */
    void waitForMoreTask(long waitInMs);

    WnSysTask addTask(WnObj oTask, byte[] input) throws WnSysTaskException;

    void removeTask(WnObj oTask) throws WnSysTaskException;

    /**
     * 运行一个任务对象
     * 
     * @param runer
     *            命令执行类
     * @param oTask
     *            任务对象
     * @param user
     *            要切换的用户，如果不指定，则用当前线程用户权限
     * @param input
     *            命令执行的标准输入流
     * @throws WnSysTaskException
     */
    void runTask(WnAuthExecutable runer, WnObj oTask, WnUser user, InputStream input)
            throws WnSysTaskException;

    /**
     * 根据查询条件，弹出一个任务对象
     * 
     * @param query
     *            查询条件
     * @return 任务对象（包括元数据，和内容）。 <code>null</code>表示没有更多任务对象了
     */
    WnSysTask popTask(WnSysTaskQuery query) throws WnSysTaskException;

    WnObj checkTask(String id);

    /**
     * 列出所有后台任务，按时间从早到晚排序
     * 
     * @param query
     *            查询条件
     * @return 任务对象列表
     */
    List<WnObj> listTasks(WnSysTaskQuery query);

}