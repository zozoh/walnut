package org.nutz.walnut.ext.sys.schedule.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.sys.cron.WnSysCron;
import org.nutz.walnut.ext.sys.cron.WnSysCronApi;
import org.nutz.walnut.ext.sys.schedule.WnCronSlot;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleApi;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleQuery;
import org.nutz.walnut.ext.sys.schedule.cmd_schedule;
import org.nutz.walnut.ext.sys.schedule.bean.WnMinuteSlotIndex;
import org.nutz.walnut.ext.sys.task.WnSysTask;
import org.nutz.walnut.ext.sys.task.WnSysTaskApi;
import org.nutz.walnut.ext.sys.task.WnSysTaskException;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.Wnum;
import org.nutz.walnut.util.Wtime;

/**
 * 系统分钟计划任务服务类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSysScheduleService implements WnSysScheduleApi {

    private WnIo io;

    private WnAuthService auth;

    private WnSysCronApi cronApi;

    private WnSysTaskApi taskApi;

    /* AUDO get by init */
    private WnObj scheduleHome;

    /* AUDO get by rootUser */
    private WnAccount rootUser;

    public WnSysScheduleService() {}

    @Override
    public List<WnObj> listSlotObj(WnSysScheduleQuery query, boolean loadContent) {
        // 准备操作任务的列表
        WnObj home = scheduleHome;
        WnContext wc = Wn.WC();

        // 进入内核态，查询相应任务
        List<WnObj> list = wc.nosecurity(io, new Proton<List<WnObj>>() {
            protected List<WnObj> exec() {
                WnQuery q = Wn.Q.pid(home);
                query.joinQuery(q);
                q.sortBy("slot", 1);
                List<WnObj> list = io.query(q);

                // 要读取内容
                if (loadContent && !list.isEmpty()) {
                    for (WnObj o : list) {
                        String cmdText = io.readText(o);
                        o.put("content", cmdText);
                    }
                }

                // 搞定，返回
                return list;
            }
        });

        // 搞定
        return list;
    }

    @Override
    public List<WnSysTask> pushSchedule(List<WnCronSlot> slots, boolean keep)
            throws WnSysTaskException {
        // 准备返回列表
        List<WnSysTask> re = new ArrayList<>(slots.size());
        List<WnCronSlot> dels = new ArrayList<>(slots.size());
        // 转换对象并执行插入
        for (WnCronSlot slot : slots) {
            WnObj oTask = slot.toTaskObj();
            WnSysTask task = taskApi.addTask(oTask, null);
            re.add(task);
            dels.add(slot);
        }
        // 是否要删除所有时间槽呢？
        if (!keep && dels.size() > 0) {
            WnContext wc = Wn.WC();
            wc.nosecurity(io, new Atom() {
                public void run() {
                    for (WnCronSlot slot : dels) {
                        WnObj o = slot.getMeta();
                        io.delete(o);
                    }
                }
            });
        }
        // 返回
        return re;
    }

    @Override
    public List<WnObj> cleanSlotObj(WnSysScheduleQuery query) {
        // 准备操作任务的列表
        WnObj home = scheduleHome;
        WnContext wc = Wn.WC();

        // 进入内核态，查询相应任务
        List<WnObj> list = wc.nosecurity(io, new Proton<List<WnObj>>() {
            protected List<WnObj> exec() {
                // 删除索引文件
                String fph = getScheduleIndexFilePath(query.getToday());
                WnObj oIndex = io.fetch(scheduleHome, fph);
                if (null != oIndex) {
                    io.delete(oIndex);
                }

                // 删除定期分钟计划任务对象
                WnQuery q = Wn.Q.pid(home);
                query.joinQuery(q);
                q.sortBy("slot", 1);
                List<WnObj> list = io.query(q);

                // 逐个删除
                for (WnObj o : list) {
                    io.delete(o);
                }

                // 搞定，返回
                return list;
            }
        });

        // 搞定
        return list;
    }

    @Override
    public List<WnCronSlot> listSlot(WnSysScheduleQuery query, boolean loadContent) {
        List<WnObj> objs = this.listSlotObj(query, loadContent);
        List<WnCronSlot> list = new ArrayList<>(objs.size());
        for (WnObj o : objs) {
            WnCronSlot slot = new WnCronSlot(o);
            list.add(slot);
        }
        return list;
    }

    @Override
    public WnMinuteSlotIndex loadSchedule(List<WnSysCron> list,
                                          Date today,
                                          String slot,
                                          int amount,
                                          boolean force) {
        // 防守
        if (null == list || list.isEmpty()) {
            return null;
        }

        // 默认为今天
        if (null == today) {
            today = Wtime.todayDate();
        }
        // 强制对齐今日 00:00:00
        else {
            Calendar c = Calendar.getInstance();
            c.setTime(today);
            Wtime.setDayStart(c);
            today = c.getTime();
        }
        Date d = today;

        // 分析起始时间槽下标
        int fromSlotIndex = cmd_schedule.timeSlotIndex(slot, 1440);
        int toSlotIndex = Wnum.clamp(fromSlotIndex + amount, 0, 1440);

        // 进入内核态运行
        WnContext wc = Wn.WC();
        WnObj home = scheduleHome;
        WnMinuteSlotIndex slotIndex = wc.core(null, true, null, new Proton<WnMinuteSlotIndex>() {
            protected WnMinuteSlotIndex exec() {
                // 得到索引文件
                String fph = getScheduleIndexFilePath(d);
                WnObj oIndex = io.createIfNoExists(scheduleHome, fph, WnRace.FILE);

                // 读取索引缓存
                WnMinuteSlotIndex slotIndex = new WnMinuteSlotIndex();
                if (!force && null != oIndex) {
                    String input = io.readText(oIndex);
                    slotIndex.fromString(input);
                }

                // 如果已经都加载过了，就直接返回吧
                if (!force) {
                    boolean loaded = true;
                    for (int i = fromSlotIndex; i < toSlotIndex; i++) {
                        if (!slotIndex.hasSlot(i)) {
                            loaded = false;
                            break;
                        }
                    }
                    if (loaded) {
                        return slotIndex;
                    }
                }

                // 设置 1 天过期时间，以便系统保证清除 push时的落网之鱼
                long msToday = d.getTime();
                long ms1day = 86400000L;

                // 准备一个返回列表
                WnSysCron[][] matrix = cronApi.previewCron(list, d, 1440);
                List<WnCronSlot> slots = new ArrayList<>(matrix.length);
                for (int y = fromSlotIndex; y < toSlotIndex; y++) {
                    WnSysCron[] row = matrix[y];
                    if (null == row) {
                        slotIndex.removeSlot(y);
                        continue;
                    }
                    // 这个分钟槽已经被加载过了，无需重新加载
                    if (!force && slotIndex.hasSlot(y)) {
                        continue;
                    }
                    // 如果是强制加载，那么首先情况这个分钟槽所有的数据
                    if (force) {
                        WnQuery q = Wn.Q.pid(home);
                        q.setv("date", msToday);
                        q.setv("slot", y);
                        List<WnObj> list = io.query(q);
                        // 逐个删除
                        for (WnObj o : list) {
                            io.delete(o);
                        }
                    }
                    // 详细加载该槽的每个任务到计划表
                    for (int x = 0; x < row.length; x++) {
                        WnSysCron cron = row[x];
                        WnCronSlot slot = new WnCronSlot(cron);
                        slot.setSlot(y);
                        slot.setDate(d);
                        slot.autoName();
                        slot.setExpi(msToday + y * 60 + ms1day);

                        // 持久化
                        WnObj o = io.create(scheduleHome, slot.getName(), WnRace.FILE);
                        NutMap meta = new NutMap();
                        slot.updateBeanFields(meta);
                        slot.setMeta(o);
                        io.appendMeta(o, meta);

                        // 写入命令内容
                        io.copyData(cron.getMeta(), slot.getMeta());

                        // 加入结果
                        slots.add(slot);
                    }
                    // 设置到索引
                    int taskCount = row.length;
                    slotIndex.setSlot(y, taskCount);
                }

                // 重新设置索引文件的过期时间（2天）
                io.appendMeta(oIndex, Wlang.map("expi", msToday + ms1day * 2));

                // 写入索引文件内容
                String str = slotIndex.toString();
                io.writeText(oIndex, str);

                // 搞定
                return slotIndex;
            }
        });

        // 返回结果
        return slotIndex;
    }

    /**
     * @param today
     *            日期
     * @return 分钟计划表索引文件的路径（相对于HOME）
     */
    private String getScheduleIndexFilePath(Date today) {
        String fnm = Times.format("yyyyMMdd", today);
        fnm += ".schedule.index.txt";
        String fph = "index/" + fnm;
        return fph;
    }

    /**
     * 创建时，初始化内部属性
     */
    public void on_create() {
        if (null == this.rootUser) {
            this.rootUser = auth.getAccount("root");
        }
        if (null == this.scheduleHome) {
            this.scheduleHome = io.createIfNoExists(null, "/sys/schedule/", WnRace.DIR);
        }
    }

}
