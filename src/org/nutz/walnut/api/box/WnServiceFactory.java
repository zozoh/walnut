package org.nutz.walnut.api.box;

import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.ext.sys.cron.WnSysCronService;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleService;
import org.nutz.walnut.ext.sys.task.WnSysTaskService;

public class WnServiceFactory {

    private WnAuthService authApi;

    private WnSysTaskService taskApi;

    private WnSysCronService cronApi;

    private WnSysScheduleService scheduleApi;

    private WnBoxService boxApi;

    public WnAuthService getAuthApi() {
        return authApi;
    }

    public WnSysTaskService getTaskApi() {
        return taskApi;
    }

    public WnSysCronService getCronApi() {
        return cronApi;
    }

    public WnSysScheduleService getScheduleApi() {
        return scheduleApi;
    }

    public WnBoxService getBoxApi() {
        return boxApi;
    }

}
