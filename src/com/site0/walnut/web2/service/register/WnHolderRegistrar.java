package com.site0.walnut.web2.service.register;

import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.ext.sys.cron.WnSysCronApi;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleApi;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.impl.box.JvmBoxService;
import com.site0.walnut.impl.box.JvmExecutorFactory;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.util.WnRun;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配产物 {@code WnServiceHolder}（旧 bean 名 {@code serviceFactory}）与
 * BGT 执行入口 {@link WnRun}（旧路径由 {@code @IocBean} 装配）。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnHolderRegistrar {

    private WnHolderRegistrar() {}

    /**
     * @return 组装好的 BGT 执行入口（不注册为 bean，由工厂持有）
     */
    public static WnRun register(WnServiceFactory factory) {
        WnConfig conf = factory.getConfig();

        WnHookService hookService = factory.fetch("hookService", WnHookService.class);
        WnLoginApi loginApi = factory.fetch("sysLoginApi", WnLoginApi.class);
        WnSysTaskApi safeTaskApi = factory.fetch("safeSysTaskService", WnSysTaskApi.class);
        WnSysScheduleApi safeScheduleApi = factory.fetch("safeSysScheduleService",
                                                         WnSysScheduleApi.class);
        WnSysCronApi cronApi = factory.fetch("sysCronService", WnSysCronApi.class);
        JvmBoxService boxService = factory.fetch("boxService", JvmBoxService.class);
        WnReferApi referApi = factory.fetch("referApi", WnReferApi.class);
        WnLockApi lockApi = factory.fetch("lockApi", WnLockApi.class);
        WnIo io = factory.fetch("io", WnIo.class);
        JvmExecutorFactory jef = factory.fetch("jvmExecutorFactory", JvmExecutorFactory.class);

        // 产物：WnServiceHolder（bean 名沿用旧 ioc 的 "serviceFactory"）
        WnServiceHolder holder = new WnServiceHolder();
        holder.setConfig(conf);
        holder.setHookApi(hookService);
        holder.setLoginApi(loginApi);
        holder.setTaskApi(safeTaskApi);
        holder.setScheduleApi(safeScheduleApi);
        holder.setCronApi(cronApi);
        holder.setBoxApi(boxService);
        holder.setReferApi(referApi);
        holder.setLockApi(lockApi);
        factory.register("serviceFactory", holder);

        // WnRun 是 BGT 任务线程的执行入口（旧路径由 @IocBean 装配；web2 程序化注入）
        WnRun run = new WnRun();
        run.setServices(holder);
        run.setIo(io);
        run.setTaskApi(factory.fetch("sysTaskService", WnSysTaskApi.class));
        run.setCronApi(cronApi);
        run.setScheduleApi(factory.fetch("sysScheduleService", WnSysScheduleApi.class));
        run.setAuth(loginApi);
        run.setJef(jef);
        run.setBoxes(boxService);
        run.setHooks(hookService);
        run.setAllocTimeout(conf.getInt("box-alloc-timeout"));
        run.setConf(conf);
        return run;
    }

}
