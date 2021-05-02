package org.nutz.walnut.ext.sys.task.impl;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.Callback;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.WnAuthExecutable;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.sys.task.WnSysTask;
import org.nutz.walnut.ext.sys.task.WnSysTaskApi;
import org.nutz.walnut.ext.sys.task.WnSysTaskException;
import org.nutz.walnut.ext.sys.task.WnSysTaskQuery;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.Ws;

/**
 * 系统命令服务类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSysTaskService implements WnSysTaskApi {

    private WnIo io;

    private WnAuthService auth;

    /* AUDO get by init */
    private WnObj taskHome;

    /* AUDO get by rootUser */
    private WnAccount rootUser;

    public WnSysTaskService() {}

    @Override
    public void runTask(WnAuthExecutable runer, WnObj oTask, WnAccount user, InputStream input)
            throws WnSysTaskException {
        // 准备命令
        String cmdText = oTask.getString("command");

        if (Ws.isBlank(cmdText)) {
            throw new WnSysTaskException("blank task");
        }

        // 采用自己的账号执行
        if (null == user) {
            runer.exec(cmdText, null, null, input);
        }
        // 切换到目标账号执行
        else {
            runer.switchUser(user, new Callback<WnAuthExecutable>() {
                public void invoke(WnAuthExecutable sys2) {
                    sys2.exec(cmdText, null, null, input);
                }
            });
        }
    }

    @Override
    public WnSysTask addTask(WnObj oTask, byte[] input) {
        WnObj home = this.taskHome;
        oTask.setParent(home);
        oTask.race(WnRace.FILE);
        if (!oTask.isType("^(task|cron)$")) {
            oTask.type("task");
        }
        WnContext wc = Wn.WC();
        WnObj o = wc.core(null, true, null, new Proton<WnObj>() {
            protected WnObj exec() {
                WnObj o = io.create(home, oTask);
                if (null != input && input.length > 0) {
                    io.writeBytes(o, input);
                }
                return o;
            }
        });
        return new WnSysTask(o, input);
    }

    public WnObj getTask(String id) {
        WnContext wc = Wn.WC();
        return wc.nosecurity(io, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.get(id);
            }
        });
    }

    @Override
    public WnObj checkTask(String id) {
        WnObj oTask = this.getTask(id);
        if (null == oTask) {
            throw Er.create("e.sys.task.noexits", id);
        }
        return oTask;
    }

    @Override
    public void removeTask(WnObj oTask) {
        if (null == oTask)
            return;
        WnContext wc = Wn.WC();
        wc.core(null, true, null, new Atom() {
            public void run() {
                io.delete(oTask);
            }
        });
    }

    /**
     * 根据查询条件，弹出一个任务对象
     * 
     * @param query
     *            查询条件
     * @return 任务对象（包括元数据，和内容）。 <code>null</code>表示没有更多任务对象了
     */
    @Override
    public WnSysTask popTask(WnSysTaskQuery query) {
        // 准备操作任务的列表
        WnObj home = this.taskHome;
        WnContext wc = Wn.WC();

        // 进入内核态，查询相应任务
        List<WnObj> list = wc.nosecurity(io, new Proton<List<WnObj>>() {
            protected List<WnObj> exec() {
                // 逐个 ID 列表
                if (query.hasIds()) {
                    List<WnObj> list = new LinkedList<>();
                    for (String id : query.getIds()) {
                        WnObj oTask = io.checkById(id);
                        // 如果限定了用户，那么 ID 指定的任务，也必须符合这个设定
                        if (query.hasUserName()) {
                            String unm = query.getUserName();
                            if (!unm.equals(oTask.creator())) {
                                continue;
                            }
                        }
                        list.add(oTask);
                        break;
                    }
                    return list;
                }
                // 范查
                WnQuery q = Wn.Q.pid(home);
                query.joinQuery(q);
                q.sortBy("ct", -1);
                q.limit(1);
                return io.query(q);
            }
        });

        // 搞定
        if (!list.isEmpty()) {
            WnObj oTask = list.get(0);
            byte[] input = io.readBytes(oTask);
            // 因为是弹出，所以删除
            this.removeTask(oTask);
            // 返回给调用者
            return new WnSysTask(oTask, input);
        }
        // 那么就是木有任务了哦
        return null;
    }

    /**
     * 列出所有后台任务，按时间从早到晚排序
     * 
     * @param query
     *            查询条件
     * @return 任务对象列表
     */
    @Override
    public List<WnObj> listTasks(WnSysTaskQuery query) {
        // 准备操作任务的列表
        WnObj home = this.taskHome;
        WnContext wc = Wn.WC();

        // 进入内核态，查询相应任务
        List<WnObj> list = wc.nosecurity(io, new Proton<List<WnObj>>() {
            protected List<WnObj> exec() {
                // 逐个 ID 列表
                if (query.hasIds()) {
                    List<WnObj> list = new LinkedList<>();
                    for (String id : query.getIds()) {
                        WnObj oTask = io.checkById(id);
                        // 如果限定了用户，那么 ID 指定的任务，也必须符合这个设定
                        if (query.hasUserName()) {
                            String unm = query.getUserName();
                            if (!unm.equals(oTask.creator())) {
                                continue;
                            }
                        }
                        list.add(oTask);
                    }
                    return list;
                }
                // 范查
                WnQuery q = Wn.Q.pid(home);
                query.joinQuery(q);
                q.sortBy("ct", -1);
                return io.query(q);
            }
        });

        // 搞定
        return list;
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public WnAuthService getAuth() {
        return auth;
    }

    public void setAuth(WnAuthService auth) {
        this.auth = auth;
    }

    public WnAccount getRootUser() {
        return rootUser;
    }

    public void setRootUser(WnAccount rootUser) {
        this.rootUser = rootUser;
    }

    public WnObj getTaskHome() {
        return taskHome;
    }

    public void setTaskHome(WnObj taskHome) {
        this.taskHome = taskHome;
    }

    /**
     * 创建时，初始化内部属性
     */
    public void on_create() {
        if (null == this.rootUser) {
            this.rootUser = auth.getAccount("root");
        }
        if (null == this.taskHome) {
            this.taskHome = io.createIfNoExists(null, "/sys/tasks/", WnRace.DIR);
        }
    }
}
