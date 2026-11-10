package com.site0.walnut.web2.service.register;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.impl.box.JvmBoxService;
import com.site0.walnut.impl.box.JvmExecutorFactory;
import com.site0.walnut.impl.hook.CachedWnHookService;
import com.site0.walnut.lookup.impl.WnLookupMaker;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.service.WnServiceFactory;

/**
 * 装配沙箱/钩子/查找器：{@code jvmExecutorFactory}/{@code boxService}/
 * {@code hookService}/{@code lookupMaker}（旧 {@code service.js} 片段）。
 * <p>
 * {@code ElasticsearchService} 已按用户 2026-11-09 的决定弃用，因此
 * {@code hookService.esi} 不注入（esi 钩子不再可用）。
 * <p>
 * 仅装配期使用：无状态、无单例，不是运行期服务入口。
 */
public final class WnBoxRegistrar {

    private WnBoxRegistrar() {}

    public static void register(WnServiceFactory factory) {
        WnConfig conf = factory.getConfig();
        WnIo io = factory.fetch("io", WnIo.class);

        JvmExecutorFactory jvmExecutorFactory = new JvmExecutorFactory();
        jvmExecutorFactory.setScanPkgs(conf.getJvmboxPkgs());
        // 说明：JvmExecutorFactory.ioc 的类型是 org.nutz.ioc.Ioc，web2 禁止 import；
        // 该字段留空，能力门/命令执行器改造在 RM-NUTZ-05/13 落地。
        factory.register("jvmExecutorFactory", jvmExecutorFactory);

        factory.register("boxService", new JvmBoxService(jvmExecutorFactory));

        factory.register("hookService", new CachedWnHookService(io));

        factory.register("lookupMaker", new WnLookupMaker(io));
    }

}
