package org.nutz.walnut.ext.sys.schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.sys.cron.WnSysCron;
import org.nutz.walnut.ext.sys.cron.WnSysCronService;
import org.nutz.walnut.ext.sys.task.WnSysTaskService;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.Ws;

/**
 * 系统分钟计划任务服务类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnSysScheduleService {

    private WnIo io;

    private WnAuthService auth;

    private WnSysCronService cronApi;

    private WnSysTaskService taskApi;

    /* AUDO get by init */
    private WnObj scheduleHome;

    /* AUDO get by rootUser */
    private WnAccount rootUser;

    public WnSysScheduleService() {}

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

    public List<WnCronSlot> listSlot(WnSysScheduleQuery query, boolean loadContent) {
        List<WnObj> objs = this.listSlotObj(query, loadContent);
        List<WnCronSlot> list = new ArrayList<>(objs.size());
        for (WnObj o : objs) {
            WnCronSlot slot = new WnCronSlot(o);
            list.add(slot);
        }
        return list;
    }

    public List<WnCronSlot> loadSchedule(List<WnSysCron> list, Date today, boolean force) {
        WnContext wc = Wn.WC();
        List<WnCronSlot> slots = wc.core(null, true, null, new Proton<List<WnCronSlot>>() {
            protected List<WnCronSlot> exec() {

                // 得到索引文件
                List<WnCronSlot> slots;
                String fnm = Times.format("yyyyMMdd", today);
                fnm += ".schedule.index.txt";
                String fph = "index/" + fnm;
                WnObj oIndex = io.fetch(scheduleHome, fph);

                // 读取索引缓存
                if (!force && null != oIndex) {
                    String input = io.readText(oIndex);
                    String[] lines = Ws.splitIgnoreBlank(input, "\r?\n");
                    slots = new ArrayList<>(lines.length);
                    for (String line : lines) {
                        slots.add(new WnCronSlot(line));
                    }
                    return slots;
                }

                // 设置 3 天过期时间
                long todayMs = today.getTime();
                long expi = todayMs + 86400000L * 3;

                // 准备一个返回列表
                WnSysCron[][] matrix = cronApi.previewCron(list, today);
                slots = new ArrayList<>(matrix.length);
                for (int y = 0; y < matrix.length; y++) {
                    WnSysCron[] cells = matrix[y];
                    for (int x = 0; x < cells.length; x++) {
                        WnSysCron cron = cells[x];
                        WnCronSlot slot = new WnCronSlot(cron);
                        slot.setSlot(y);
                        slot.setDate(today);
                        slot.autoName();
                        slot.setExpi(expi);

                        // 持久化
                        WnObj o = io.create(scheduleHome, slot.getName(), WnRace.FILE);
                        NutMap meta = new NutMap();
                        slot.updateBeanFields(meta);
                        slot.setMeta(o);
                        io.appendMeta(o, meta);

                        // 加入返回结果
                        slots.add(slot);
                    }
                }

                // 写入索引文件
                if (null == oIndex) {
                    oIndex = io.createIfNoExists(scheduleHome, fph, WnRace.FILE);
                }
                StringBuilder sb = new StringBuilder();
                for (WnCronSlot slot : slots) {
                    sb.append(slot.toString()).append('\n');
                }
                io.writeText(oIndex, sb);

                // 搞定
                return slots;
            }
        });

        // 返回结果
        return slots;
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
