package org.nutz.walnut.api.box;

import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.hook.WnHookService;
import org.nutz.walnut.core.WnReferApi;
import org.nutz.walnut.ext.sys.cron.WnSysCronApi;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleApi;
import org.nutz.walnut.ext.sys.task.WnSysTaskApi;

public class WnServiceFactory {

    private WnHookService hookApi;

    private WnAuthService authApi;

    private WnSysTaskApi taskApi;

    private WnSysScheduleApi scheduleApi;

    private WnSysCronApi cronApi;

    private WnBoxService boxApi;

    private WnReferApi referApi;

    public WnAuthService getAuthApi() {
        return authApi;
    }

    public WnSysTaskApi getTaskApi() {
        return taskApi;
    }

    public WnSysScheduleApi getScheduleApi() {
        return scheduleApi;
    }

    public WnSysCronApi getCronApi() {
        return cronApi;
    }

    public WnBoxService getBoxApi() {
        return boxApi;
    }

    public WnHookService getHookApi() {
        return hookApi;
    }

    public WnReferApi getReferApi() {
        return referApi;
    }

}
