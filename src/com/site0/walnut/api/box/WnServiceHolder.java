package com.site0.walnut.api.box;

import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.core.bm.bml.LocalSha1BM;
import com.site0.walnut.ext.sys.cron.WnSysCronApi;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleApi;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.lookup.impl.WnLookupMaker;
import com.site0.walnut.util.MongoDB;
import com.site0.walnut.util.WnConfig;
import com.site0.walnut.web2.controller.app.WnAppService;

public class WnServiceHolder {

    private WnIo io;

    private MongoDB mongoDB;

    private LocalSha1BM globalBM;

    private WnHookService hookApi;

    private WnLoginApi loginApi;

    private WnSysTaskApi taskApi;

    private WnSysScheduleApi scheduleApi;

    private WnSysCronApi cronApi;

    private WnBoxService boxApi;

    private WnReferApi referApi;

    private WnLockApi lockApi;

    private WnConfig config;

    private WnAppService appService;

    private WnLookupMaker lookupMaker;

    public WnAppService getAppService() {
        return appService;
    }

    public void setAppService(WnAppService appService) {
        this.appService = appService;
    }

    public WnLookupMaker getLookupMaker() {
        return lookupMaker;
    }

    public void setLookupMaker(WnLookupMaker lookupMaker) {
        this.lookupMaker = lookupMaker;
    }

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

    public WnConfig getConfig() {
        return config;
    }

    public void setConfig(WnConfig config) {
        this.config = config;
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public MongoDB getMongoDB() {
        return mongoDB;
    }

    public void setMongoDB(MongoDB mongoDB) {
        this.mongoDB = mongoDB;
    }

    public LocalSha1BM getGlobalBM() {
        return globalBM;
    }

    public void setGlobalBM(LocalSha1BM globalBM) {
        this.globalBM = globalBM;
    }

}
