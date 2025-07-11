package com.site0.walnut.api.box;

import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.ext.sys.cron.WnSysCronApi;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleApi;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.login.WnLoginApi;

public class WnServiceFactory {

    private WnHookService hookApi;

    private WnLoginApi loginApi;

    private WnSysTaskApi taskApi;

    private WnSysScheduleApi scheduleApi;

    private WnSysCronApi cronApi;

    private WnBoxService boxApi;

    private WnReferApi referApi;

    private WnLockApi lockApi;

    public WnLockApi getLockApi() {
        return lockApi;
    }

    public WnLoginApi getLoginApi() {
        return loginApi;
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

    public void setHookApi(WnHookService hookApi) {
        this.hookApi = hookApi;
    }

    public void setLoginApi(WnLoginApi loginApi) {
        this.loginApi = loginApi;
    }

    public void setTaskApi(WnSysTaskApi taskApi) {
        this.taskApi = taskApi;
    }

    public void setScheduleApi(WnSysScheduleApi scheduleApi) {
        this.scheduleApi = scheduleApi;
    }

    public void setCronApi(WnSysCronApi cronApi) {
        this.cronApi = cronApi;
    }

    public void setBoxApi(WnBoxService boxApi) {
        this.boxApi = boxApi;
    }

    public void setReferApi(WnReferApi referApi) {
        this.referApi = referApi;
    }

    public void setLockApi(WnLockApi lockApi) {
        this.lockApi = lockApi;
    }

    
    
}
