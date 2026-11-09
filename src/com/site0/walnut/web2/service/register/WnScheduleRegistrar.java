package com.site0.walnut.web2.service.register;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.ext.sys.cron.WnSysCronApi;
import com.site0.walnut.ext.sys.schedule.impl.WnSafeSysScheduleService;
import com.site0.walnut.ext.sys.schedule.impl.WnSysScheduleService;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.ext.sys.task.impl.WnSafeSysTaskService;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配 {@code sysScheduleService} + {@code safeSysScheduleService}
 * （旧 {@code service.js} 片段）。依赖已注册的 task/cron 服务与 lockApi。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnScheduleRegistrar {

    private WnScheduleRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnConfig conf = factory.getConfig();
        WnIo io = factory.fetch("io", WnIo.class);
        WnLoginApi auth = factory.fetch("sysLoginApi", WnLoginApi.class);
        WnLockApi lockApi = factory.fetch("lockApi", WnLockApi.class);
        WnSysCronApi cronApi = factory.fetch("sysCronService", WnSysCronApi.class);
        WnSysTaskApi taskApi = factory.fetch("sysTaskService", WnSysTaskApi.class);
        WnSafeSysTaskService safeSysTaskService = factory.fetch("safeSysTaskService",
                                                               WnSafeSysTaskService.class);

        WnSysScheduleService sysScheduleService = new WnSysScheduleService();
        sysScheduleService.setIo(io);
        sysScheduleService.setAuth(auth);
        sysScheduleService.setCronApi(cronApi);
        sysScheduleService.setTaskApi(taskApi);
        factory.register("sysScheduleService", sysScheduleService);

        WnSafeSysScheduleService safeSysScheduleService = new WnSafeSysScheduleService();
        safeSysScheduleService.setSchedules(sysScheduleService);
        safeSysScheduleService.setLocks(lockApi);
        safeSysScheduleService.setTryLockDuration(conf.getLong("sys-schedule-lockdu", 60000));
        safeSysScheduleService.setTryAddTaskLockDuration(conf.getLong("sys-schedule-task-lockdu",
                                                                     10000));
        safeSysScheduleService.setTaskNotifyLock(safeSysTaskService);
        factory.register("safeSysScheduleService", safeSysScheduleService);
    }

}
