package com.site0.walnut.web2.service.register;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.ext.sys.task.impl.WnSafeSysTaskService;
import com.site0.walnut.ext.sys.task.impl.WnSysTaskService;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配 {@code sysTaskService} + {@code safeSysTaskService}（旧 {@code service.js} 片段）。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnTaskRegistrar {

    private WnTaskRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnConfig conf = factory.getConfig();
        WnIo io = factory.fetch("io", WnIo.class);
        WnLoginApi auth = factory.fetch("sysLoginApi", WnLoginApi.class);
        WnLockApi lockApi = factory.fetch("lockApi", WnLockApi.class);

        WnSysTaskService sysTaskService = new WnSysTaskService();
        sysTaskService.setIo(io);
        sysTaskService.setAuth(auth);
        factory.register("sysTaskService", sysTaskService);

        WnSafeSysTaskService safeSysTaskService = new WnSafeSysTaskService();
        safeSysTaskService.setTasks(sysTaskService);
        safeSysTaskService.setLocks(lockApi);
        safeSysTaskService.setTryLockDuration(conf.getLong("sys-task-lockdu", 3000));
        factory.register("safeSysTaskService", safeSysTaskService);
    }

}
