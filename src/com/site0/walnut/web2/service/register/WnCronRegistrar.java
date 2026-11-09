package com.site0.walnut.web2.service.register;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.sys.cron.impl.WnSysCronService;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配 {@code sysCronService}（旧 {@code service.js} 片段）。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnCronRegistrar {

    private WnCronRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnIo io = factory.fetch("io", WnIo.class);
        WnLoginApi auth = factory.fetch("sysLoginApi", WnLoginApi.class);

        WnSysCronService sysCronService = new WnSysCronService();
        sysCronService.setIo(io);
        sysCronService.setAuth(auth);
        factory.register("sysCronService", sysCronService);
    }

}
