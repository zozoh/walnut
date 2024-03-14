package com.site0.walnut.ext.sys.schedule;

import java.util.Date;
import java.util.List;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.cron.WnSysCron;
import com.site0.walnut.ext.sys.schedule.bean.WnMinuteSlotIndex;
import com.site0.walnut.ext.sys.task.WnSysTask;
import com.site0.walnut.ext.sys.task.WnSysTaskException;

public interface WnSysScheduleApi {

    List<WnObj> cleanSlotObj(WnSysScheduleQuery query) throws WnSysScheduleException;

    List<WnMinuteSlotIndex> loadSchedule(List<WnSysCron> list,
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