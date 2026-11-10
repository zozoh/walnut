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
import com.site0.walnut.util.MongoDB;
import com.site0.walnut.util.WnConfig;

/**
 * 执行器/命令的服务访问器（RM-NUTZ-11）。
 * <p>
 * 只读封装 {@link WnServiceHolder}：把旧命令里的 {@code hc.ioc.get(X.class, "bean")}
 * 改成类型化 getter，不暴露注册表、不提供 {@code get(Class,name)}。
 */
public class WnServiceGetter {

    private final WnServiceHolder services;

    public WnServiceGetter(WnServiceHolder services) {
        this.services = services;
    }

    public WnServiceHolder getServices() {
        return services;
    }

    public WnConfig getConfig() {
        return services.getConfig();
    }

    public WnIo getIo() {
        return services.getIo();
    }

    public WnLoginApi getLoginApi() {
        return services.getLoginApi();
    }

    public WnSysTaskApi getTaskApi() {
        return services.getTaskApi();
    }

    public WnSysScheduleApi getScheduleApi() {
        return services.getScheduleApi();
    }

    public WnSysCronApi getCronApi() {
        return services.getCronApi();
    }

    public WnBoxService getBoxApi() {
        return services.getBoxApi();
    }

    public WnHookService getHookApi() {
        return services.getHookApi();
    }

    public WnReferApi getReferApi() {
        return services.getReferApi();
    }

    public WnLockApi getLockApi() {
        return services.getLockApi();
    }

    public MongoDB getMongoDB() {
        return services.getMongoDB();
    }

    public LocalSha1BM getGlobalBM() {
        return services.getGlobalBM();
    }

}
