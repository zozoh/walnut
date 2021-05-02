package org.nutz.walnut.ext.sys.schedule;

import java.util.Date;
import java.util.List;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.cron.WnSysCron;
import org.nutz.walnut.ext.sys.schedule.bean.WnMinuteSlotIndex;
import org.nutz.walnut.ext.sys.task.WnSysTask;
import org.nutz.walnut.ext.sys.task.WnSysTaskException;

public interface WnSysScheduleApi {

    List<WnObj> cleanSlotObj(WnSysScheduleQuery query) throws WnSysScheduleException;

    WnMinuteSlotIndex loadSchedule(List<WnSysCron> list,
                                   Date today,
                                   String slot,
                                   int amount,
                                   boolean force)
            throws WnSysScheduleException;

    List<WnSysTask> pushSchedule(List<WnCronSlot> slots, boolean keep)
            throws WnSysTaskException, WnSysScheduleException;

    List<WnObj> listSlotObj(WnSysScheduleQuery query, boolean loadContent);

    List<WnCronSlot> listSlot(WnSysScheduleQuery query, boolean loadContent);
}