package org.nutz.walnut.ext.sys.cron;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.cron.CronOverlapor;
import org.nutz.walnut.cron.WnCron;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

/**
 * 系统定期任务服务类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSysCronService {

    private WnIo io;

    private WnAuthService auth;

    /* AUDO get by init */
    private WnObj cronHome;

    /* AUDO get by rootUser */
    private WnAccount rootUser;

    public WnSysCronService() {}

    public WnObj addCron(WnSysCron cron) {
        // 如果数据不对，就没必要了
        if (!cron.isValid()) {
            return null;
        }

        // 执行 cron 对象的创建
        WnObj home = this.cronHome;
        WnContext wc = Wn.WC();
        WnObj o = wc.core(null, true, null, new Proton<WnObj>() {
            protected WnObj exec() {
                // 确保有对应用户
                auth.checkAccount(cron.getUser());

                // 准备创建对象
                WnObj oCron = new WnIoObj();
                oCron.setParent(home);
                oCron.race(WnRace.FILE);
                cron.updateBeanFields(oCron);

                // 执行创建
                WnObj o = io.create(home, oCron);
                io.writeText(o, cron.getCommand());
                return o;
            }
        });

        // 再次更新
        cron.loadFromObj(o);

        // 返回对象
        return o;
    }

    public WnSysCron getCronById(String id) {
        WnContext wc = Wn.WC();
        WnObj oCron = wc.nosecurity(io, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.getIn(cronHome, id);
            }
        });
        return new WnSysCron(oCron);
    }

    public WnSysCron checkCronById(String id) {
        WnSysCron cron = this.getCronById(id);
        if (null == cron) {
            throw Er.create("e.sys.cron.id.noexits", id);
        }
        return cron;
    }

    public WnSysCron getCron(String name) {
        WnContext wc = Wn.WC();
        WnObj oCron = wc.nosecurity(io, new Proton<WnObj>() {
            protected WnObj exec() {
                return io.fetch(cronHome, name);
            }
        });
        return new WnSysCron(oCron);
    }

    public WnSysCron checkCron(String name) {
        WnSysCron cron = this.getCron(name);
        if (null == cron) {
            throw Er.create("e.sys.cron.name.noexits", name);
        }
        return cron;
    }

    public void removeCron(WnSysCron cron) {
        this.removeCronObj(cron.getMeta());
    }

    public void removeCronObj(WnObj oCron) {
        if (null == oCron) {
            return;
        }
        WnContext wc = Wn.WC();
        wc.core(null, true, null, new Atom() {
            public void run() {
                io.delete(oCron);
            }
        });
    }

    public List<WnObj> listCronObj(WnSysCronQuery query, boolean loadContent) {
        // 准备操作任务的列表
        WnObj home = this.cronHome;
        WnContext wc = Wn.WC();

        // 进入内核态，查询相应任务
        List<WnObj> list = wc.nosecurity(io, new Proton<List<WnObj>>() {
            protected List<WnObj> exec() {
                List<WnObj> list;
                // 逐个 ID 列表
                if (query.hasIds()) {
                    list = new LinkedList<>();
                    for (String id : query.getIds()) {
                        WnObj oTask = io.checkById(id);
                        // 如果限定了用户，那么 ID 指定的任务，也必须符合这个设定
                        if (query.hasUserName()) {
                            String unm = query.getUserName();
                            String taskUser = oTask.getString("user");
                            if (!unm.equals(taskUser)) {
                                continue;
                            }
                        }
                        list.add(oTask);
                    }
                }
                // 范查
                else {
                    WnQuery q = Wn.Q.pid(home);
                    query.joinQuery(q);
                    q.sortBy("ct", -1);
                    list = io.query(q);
                }

                // 要读取内容
                if (loadContent && !list.isEmpty()) {
                    for (WnObj o : list) {
                        String content = io.readText(o);
                        o.put("content", content);
                    }
                }

                // 搞定，返回
                return list;
            }
        });

        // 搞定
        return list;
    }

    public List<WnSysCron> listCron(WnSysCronQuery query, boolean loadContent) {
        List<WnObj> objs = this.listCronObj(query, loadContent);
        List<WnSysCron> list = new ArrayList<>(objs.size());
        for (WnObj oCron : objs) {
            WnSysCron cron = new WnSysCron(oCron);
            list.add(cron);
        }
        return list;
    }

    /**
     * 从给定的定期任务对象列表，根据每个对象的cron表达式，将自己展开到一个二维数组。
     * <p>
     * 这个矩阵格式是：
     * 
     * <pre>
     * MATRIX[分钟槽下标][任务下标】
     * </pre>
     * 
     * 譬如:
     * 
     * <pre>
     * 0:  [TASK][TASK][TASK]
     * 1:  null
     * 2:  null
     * 3:  [TASK]
     * ...
     * </pre>
     * 
     * 也就是说，如果<code>MATRIX[2][0]</code> 表示访问当天的第3分钟的第0个任务
     * <p>
     * <b>!!!注意</b>，分钟槽的内容可能是 NULL 或者空数组，都表示这个分钟槽没有任何任务。
     * <p>
     * 切记！切记！否则出了<code>NPE</code>我可不背锅。
     * 
     * @param list
     *            输入的定期任务
     * @param today
     *            今天几号？不是今天的任务统统会被无视。
     * @param slotN
     *            一天划分为多少个时间槽，如果精确到小时，是
     *            <code>24</code>，如果精确到分钟，是<code>1440</code>
     * @return 一个叠加好的定期任务对象矩阵
     */
    public WnSysCron[][] previewCron(List<WnSysCron> list, Date today, int slotN) {
        // 准备输出数组 1440
        int N = slotN;
        CronOverlapor[] matrix = new CronOverlapor[N];

        // 循环处理任务
        for (WnSysCron it : list) {
            // 解析定期表达式
            WnCron cron = new WnCron(it.getCron());
            cron.overlapBy(matrix, it, today);
        }

        // 转换为输出结果
        WnSysCron[][] re = new WnSysCron[N][];
        for (int i = 0; i < N; i++) {
            CronOverlapor ol = matrix[i];
            if (null != ol) {
                re[i] = ol.toArray(WnSysCron.class);
            }
        }

        // 搞定
        return re;
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

    public WnObj getCronHome() {
        return cronHome;
    }

    public void setCronHome(WnObj cronHome) {
        this.cronHome = cronHome;
    }

    public WnAccount getRootUser() {
        return rootUser;
    }

    public void setRootUser(WnAccount rootUser) {
        this.rootUser = rootUser;
    }

    /**
     * 创建时，初始化内部属性
     */
    public void on_create() {
        if (null == this.rootUser) {
            this.rootUser = auth.getAccount("root");
        }
        if (null == this.cronHome) {
            this.cronHome = io.createIfNoExists(null, "/sys/cron/", WnRace.DIR);
        }
    }

}
