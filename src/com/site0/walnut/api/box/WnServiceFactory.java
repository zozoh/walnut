package com.site0.walnut.api.box;

import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.ext.sys.cron.WnSysCronApi;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleApi;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;

public class WnServiceFactory {

    private WnHookService hookApi;

    private WnAuthService authApi;

    private WnSysTaskApi taskApi;

    private WnSysScheduleApi scheduleApi;

    private WnSysCronApi cronApi;

    private WnBoxService boxApi;

    private WnReferApi referApi;

    private WnLockApi lockApi;

    public WnLockApi getLockApi() {
        return lockApi;
    }

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
