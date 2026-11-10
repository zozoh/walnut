package com.site0.walnut.web2.service.register;

import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.impl.box.JvmExecutorFactory;
import com.site0.walnut.util.WnConfig;
import com.site0.walnut.web2.controller.app.WnAppService;
import com.site0.walnut.web2.service.WnServiceFactory;

public final class WnAppRegistrar {

    private WnAppRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnServiceHolder holder = factory.getServiceHolder();
        WnConfig conf = factory.getConfig();
        WnAppService apps = new WnAppService();
        apps.setServices(holder);
        apps.setIo(factory.fetch("io", WnIo.class));
        apps.setAuth(holder.getLoginApi());
        apps.setJef(factory.fetch("jvmExecutorFactory", JvmExecutorFactory.class));
        apps.setBoxes(holder.getBoxApi());
        apps.setHooks(holder.getHookApi());
        apps.setAllocTimeout(conf.getInt("box-alloc-timeout"));
        factory.register("appService", apps);
        holder.setAppService(apps);
        holder.setLookupMaker(factory.getLookupMaker());
    }
}
