package com.site0.walnut.web2.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.nutz.log.Log;

import com.site0.walnut.api.box.WnServiceHolder;
import com.site0.walnut.api.hook.WnHookService;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.core.WnReferApi;
import com.site0.walnut.core.bm.bml.LocalSha1BM;
import com.site0.walnut.ext.sys.cron.impl.WnSysCronService;
import com.site0.walnut.ext.sys.schedule.impl.WnSysScheduleService;
import com.site0.walnut.ext.sys.task.impl.WnSysTaskService;
import com.site0.walnut.impl.box.JvmBoxService;
import com.site0.walnut.impl.hook.CachedWnHookService;
import com.site0.walnut.login.WnSimpleLoginApi;
import com.site0.walnut.lookup.impl.WnLookupMaker;
import com.site0.walnut.util.MongoDB;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.WnRun;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web2.service.register.WnAppRegistrar;
import com.site0.walnut.web2.service.register.WnBoxRegistrar;
import com.site0.walnut.web2.service.register.WnCronRegistrar;
import com.site0.walnut.web2.service.register.WnExpiObjTableRegistrar;
import com.site0.walnut.web2.service.register.WnHolderRegistrar;
import com.site0.walnut.web2.service.register.WnIoRegistrar;
import com.site0.walnut.web2.service.register.WnLockRegistrar;
import com.site0.walnut.web2.service.register.WnLoginRegistrar;
import com.site0.walnut.web2.service.register.WnMongoRegistrar;
import com.site0.walnut.web2.service.register.WnRedisRegistrar;
import com.site0.walnut.web2.service.register.WnScheduleRegistrar;
import com.site0.walnut.web2.service.register.WnTaskRegistrar;

/**
 * web2 启动流程的非单例装配器。
 * <p>
 * 用法：{@code new WnServiceFactory(conf)}，每次 new 都会装配一套独立的服务， 产物是 {@link WnServiceHolder}（对外只给类型化
 * getter，注册表仅供内部/自检）。
 * <p>
 * 本类只负责<b>阶段编排</b>，具体"创建 + 配置 + 注册"按服务域拆到 {@code com.site0.walnut.web2.service.register} 下的静态帮助类。
 * <p>
 * 分三阶段（空库首启教训，见 RM-NUTZ-04 §4.3）：
 * <ol>
 * <li><b>构造器</b>：配置、Mongo/Redis 客户端、锁、IO 主链等不触碰账户目录的服务； 任何一步失败立即抛（fail-fast）。<b>绝不 new
 * store</b>。</li>
 * <li>{@link #initStores()}：账户/会话/角色 store、sysLoginApi、task/schedule/cron 服务及其安全包装；必须等
 * "/sys/usr"、"/var/session" 等目录已建。</li>
 * <li>{@link #initServices()}：on_create（建 "/sys/*" 目录，要求 root 存在）。</li>
 * </ol>
 * 装配清单 = {@code conf/ioc/*.js} 的 6 个文件共 58 条定义，剔除 {@code loader/webscan/ioService/sessionService}
 * 后注册 54 个 bean；逐条对照见 {@code wn-doc/plan/261108-jetty12/inventory.md} §5。 RM-NUTZ-07 在第二阶段额外装配复制版
 * {@code appService}，并写入 holder。
 * <p>
 * 边界：本包不 import {@code org.nutz.mvc/ioc/web}；{@code org.nutz.mongo.ZMoCo} 只是 Mongo 驱动封装参数类型，随
 * RM-NUTZ-12 一起迁移。
 */
public class WnServiceFactory {

    private static final Log log = Wlog.getMAIN();

    /**
     * checkup() 校验的必备清单（32 个），holder 单独校验。
     */
    private static final String[] REQUIRED = {"conf",
                                              "mongoDB",
                                              "messageQueueApi",
                                              "lockApi",
                                              "referApi",
                                              "mimes",
                                              "ioMappingFactory",
                                              "ioHandleManager",
                                              "io",
                                              "rawIo",
                                              "hookedIo",
                                              "__io",
                                              "expiObjTable",
                                              "safeExpiObjTable",
                                              "rawUserStore",
                                              "sysUserStore",
                                              "rawSessionStore",
                                              "sysSessionStore",
                                              "rawRoleStore",
                                              "sysRoleStore",
                                              "sysLoginApi",
                                              "sysXApi",
                                              "sysTaskService",
                                              "safeSysTaskService",
                                              "sysScheduleService",
                                              "safeSysScheduleService",
                                              "sysCronService",
                                              "boxService",
                                              "jvmExecutorFactory",
                                              "hookService",
                                              "lookupMaker",
                                              "appService"};

    private final WnConfig conf;

    /**
     * 注册表：bean 名 -> 实例（仅内部使用）。
     */
    private final Map<String, Object> beans;

    /**
     * 注册表：bean 名 -> 实际类型（仅内部使用）。
     */
    private final Map<String, Class<?>> beanTypes;

    /**
     * BGT 任务线程的执行入口（不属于 54 个 JS bean，不注册）。
     */
    private WnRun run;

    private boolean storesReady;

    private boolean servicesReady;

    private boolean deposed;

    /**
     * 构造器阶段：配置/连接/锁/IO 主链。不 new 任何 store。
     *
     * @param conf
     *            配置（不可为 null）
     */
    public WnServiceFactory(WnConfig conf) {
        if (null == conf) {
            throw new IllegalArgumentException("WnServiceFactory: conf is null");
        }
        this.conf = conf;
        this.beans = new LinkedHashMap<>();
        this.beanTypes = new LinkedHashMap<>();

        register("conf", conf);

        // 连接类（Mongo/Redis/MQ）
        WnMongoRegistrar.register(this);
        WnRedisRegistrar.register(this);

        // 锁，以及 IO 主链依赖的过期对象表
        WnLockRegistrar.register(this);
        WnExpiObjTableRegistrar.register(this);

        // IO 主链与映射工厂
        WnIoRegistrar.register(this);

        // 沙箱/hook/lookup
        // 注意：ElasticsearchService 已按用户 2026-11-09 的决定弃用，web2 不再实例化/注册 esi。
        WnBoxRegistrar.register(this);

        log.infof("WnServiceFactory: stage-1 done, %d beans (stores NOT created yet)",
                  beans.size());
    }

    // -------------------------------------------------------------
    // 阶段 2：store / 登录 / 任务服务
    // -------------------------------------------------------------

    /**
     * 账户/会话/角色 store、sysLoginApi、task/schedule/cron 及 safe 包装，最后产出 holder。
     * <p>
     * 调用前必须已建好 "/sys/usr"、"/sys/role"、"/var/session" 三个关键目录 （见 {@code WnWebBoot.setup()}）。
     */
    public synchronized void initStores() {
        if (storesReady) {
            return;
        }

        WnLoginRegistrar.register(this);
        WnTaskRegistrar.register(this);
        WnCronRegistrar.register(this);
        WnScheduleRegistrar.register(this);
        this.run = WnHolderRegistrar.register(this);
        WnAppRegistrar.register(this);

        storesReady = true;
        log.infof("WnServiceFactory: stage-2 done, %d beans", beans.size());
    }

    // -------------------------------------------------------------
    // 阶段 3：on_create
    // -------------------------------------------------------------

    /**
     * 调用各服务的 on_create（建 "/sys/tasks"、"/sys/cron"、"/sys/schedule" 等目录）。
     * <p>
     * 必须在 root 用户已建好、且当前上下文已 setMe(root) 之后调用。
     */
    public synchronized void initServices() {
        if (!storesReady) {
            throw new IllegalStateException("WnServiceFactory: initStores() must run before initServices()");
        }
        if (servicesReady) {
            return;
        }
        fetch("sysTaskService", WnSysTaskService.class).on_create();
        fetch("sysCronService", WnSysCronService.class).on_create();
        fetch("sysScheduleService", WnSysScheduleService.class).on_create();
        fetch("hookService", CachedWnHookService.class).on_create();
        servicesReady = true;
        log.info("WnServiceFactory: stage-3 done (on_create)");
    }

    // -------------------------------------------------------------
    // 自检 / 释放
    // -------------------------------------------------------------

    /**
     * 校验必备 bean 是否全部就位；缺一个就抛（fail-fast），否则打印清单。
     */
    public void checkup() {
        List<String> missing = new ArrayList<>();
        for (String name : REQUIRED) {
            if (!beans.containsKey(name) || null == beans.get(name)) {
                missing.add(name);
            }
        }
        if (!hasBean("serviceFactory")) {
            missing.add("serviceFactory(holder)");
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("WnServiceFactory.checkup: missing beans " + missing);
        }
        log.infof("WnServiceFactory.checkup: OK (%d required + holder, %d registered total)",
                  REQUIRED.length,
                  beans.size());
        StringBuilder sb = new StringBuilder();
        for (String name : beans.keySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        log.infof("WnServiceFactory beans: %s", sb);
    }

    /**
     * 释放 Mongo 连接；幂等。
     * <p>
     * 注：ElasticsearchService 已弃用，web2 不实例化，因此无需释放 ES。
     */
    public synchronized void depose() {
        if (deposed) {
            return;
        }
        deposed = true;
        if (hasBean("mongoDB")) {
            try {
                fetch("mongoDB", MongoDB.class).on_depose();
            }
            catch (Throwable e) {
                log.warn("WnServiceFactory: fail to depose mongoDB", e);
            }
        }
        log.info("WnServiceFactory: deposed");
    }

    // -------------------------------------------------------------
    // 注册表（仅内部/自检使用）
    // -------------------------------------------------------------

    public void register(String name, Object bean) {
        if (beans.containsKey(name)) {
            throw new IllegalStateException("WnServiceFactory: duplicate bean '" + name + "'");
        }
        beans.put(name, bean);
        beanTypes.put(name, null == bean ? null : bean.getClass());
    }

    public Object fetch(String name) {
        return beans.get(name);
    }

    /**
     * 取已注册的 bean（装配期用），未注册时抛。
     */
    public <T> T fetch(String name, Class<T> type) {
        Object bean = beans.get(name);
        if (null == bean) {
            throw new IllegalStateException("WnServiceFactory: bean '"
                                            + name
                                            + "' is not registered/ready");
        }
        return type.cast(bean);
    }

    public Class<?> getBeanType(String name) {
        return beanTypes.get(name);
    }

    public boolean hasBean(String name) {
        return beans.containsKey(name);
    }

    public List<String> getBeanNames() {
        return new ArrayList<>(beans.keySet());
    }

    // -------------------------------------------------------------
    // 类型化 getter（对外唯一入口；不提供任何静态入口）
    // -------------------------------------------------------------

    public WnConfig getConfig() {
        return conf;
    }

    public WnIo getIo() {
        return fetch("io", WnIo.class);
    }

    public MongoDB getMongoDB() {
        return fetch("mongoDB", MongoDB.class);
    }

    public LocalSha1BM getGlobalBM() {
        return fetch("globalBM", LocalSha1BM.class);
    }

    public WnServiceHolder getServiceHolder() {
        return fetch("serviceFactory", WnServiceHolder.class);
    }

    public WnSimpleLoginApi getLoginApi() {
        return fetch("sysLoginApi", WnSimpleLoginApi.class);
    }

    public WnLockApi getLockApi() {
        return fetch("lockApi", WnLockApi.class);
    }

    public WnReferApi getReferApi() {
        return fetch("referApi", WnReferApi.class);
    }

    public MimeMap getMimes() {
        return fetch("mimes", MimeMap.class);
    }

    public JvmBoxService getBoxService() {
        return fetch("boxService", JvmBoxService.class);
    }

    public WnHookService getHookService() {
        return fetch("hookService", WnHookService.class);
    }

    public WnLookupMaker getLookupMaker() {
        return fetch("lookupMaker", WnLookupMaker.class);
    }

    public WnRun getRun() {
        if (null == run) {
            throw new IllegalStateException("WnServiceFactory: initStores() has not run");
        }
        return run;
    }

}
