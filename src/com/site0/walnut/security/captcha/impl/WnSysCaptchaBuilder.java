package com.site0.walnut.security.captcha.impl;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.redis.Wedis;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.security.captcha.WnCaptchaApi;
import com.site0.walnut.security.captcha.WnCaptchaBuilder;
import com.site0.walnut.security.captcha.bean.WnCaptchaConfig;
import com.site0.walnut.security.captcha.bean.WnCaptchaStoreConfig;
import com.site0.walnut.security.captcha.impl.tianai.WnIoCaptchaResourceStore;
import com.site0.walnut.security.captcha.impl.tianai.WnIoResourceProvider;
import com.site0.walnut.security.captcha.impl.tianai.WnRedisCaptchaAnswerStore;
import com.site0.walnut.security.captcha.impl.tianai.WnRedisCaptchaTicketStore;
import com.site0.walnut.security.captcha.impl.tianai.WnTianaiCaptchaApi;
import com.site0.walnut.util.Wn;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.TACBuilder;
import cloud.tianai.captcha.generator.ImageCaptchaGenerator;
import cloud.tianai.captcha.generator.ImageTransform;
import cloud.tianai.captcha.generator.impl.MultiImageCaptchaGenerator;
import cloud.tianai.captcha.generator.impl.transform.Base64ImageTransform;
import cloud.tianai.captcha.resource.ResourceProviders;
import cloud.tianai.captcha.resource.impl.DefaultImageCaptchaResourceManager;

/**
 * captcha 工厂实现：按 <code>~/.captcha/{name}.json</code> 的配置，
 * 组装 tianai 引擎（{@link ImageCaptchaApplication}）与两个 Store，
 * 注入一个 {@link WnTianaiCaptchaApi} 返回。业务代码不直接接触引擎。
 *
 * <p>
 * 每次调用 {@link #build(String)} 都会现场组装一个新的实例，
 * 不做缓存——调用方（譬如 Web 模块）可以自行决定是否复用：
 *
 * <pre>
 * WnCaptchaBuilder builder = new WnSysCaptchaBuilder(sys);
 * WnCaptchaApi captcha = builder.build("default");
 * </pre>
 */
public class WnSysCaptchaBuilder implements WnCaptchaBuilder {

    private WnIo io;

    private NutBean vars;

    public WnSysCaptchaBuilder(WnSystem sys) {
        this(sys.io, sys.session.getEnv());
    }

    public WnSysCaptchaBuilder(WnIo io, NutBean vars) {
        this.io = io;
        this.vars = vars;
    }

    @Override
    public WnCaptchaApi build(String name) {
        return createApi(name);
    }

    private WnCaptchaApi createApi(String name) {
        // 读取配置
        WnCaptchaConfig conf = loadConfig(name);
        // 存储：答案与票据都走 Redis（与 OTP 的存储选择一致）
        WnCaptchaStoreConfig sc = conf.getStore();
        if (!"redis".equalsIgnoreCase(sc.getType())) {
            throw Er.create("e.captcha.store.UnknownType", sc.getType());
        }
        WedisConfig redisConf = Wedis.loadConfigByName(io, sc.getName(), vars);
        // 键前缀，缺省 captcha:{服务配置名}:
        String prefix = sc.hasPrefix() ? sc.getPrefix() : "captcha:" + name + ":";
        WnRedisCaptchaAnswerStore answerStore = new WnRedisCaptchaAnswerStore(redisConf, prefix);
        WnRedisCaptchaTicketStore ticketStore = new WnRedisCaptchaTicketStore(redisConf, prefix, conf.getTicketExpire());
        // 资源：背景图/字体从对象存储读，模板用内置
        WnIoCaptchaResourceStore resourceStore = new WnIoCaptchaResourceStore(io, vars, conf);
        ResourceProviders providers = new ResourceProviders();
        providers.registerResourceProvider(new WnIoResourceProvider(io, vars));
        ImageTransform transform = new Base64ImageTransform();
        ImageCaptchaGenerator generator = new MultiImageCaptchaGenerator(new DefaultImageCaptchaResourceManager(resourceStore,
                                                                                                                providers),
                                                                         transform);
        // 引擎：线程安全，单例
        ImageCaptchaApplication engine = TACBuilder.builder()
                                                   .setGenerator(generator)
                                                   .setResourceStore(resourceStore)
                                                   .setCacheStore(answerStore)
                                                   .addDefaultTemplate()
                                                   .expire("default", conf.getExpire())
                                                   .build();
        // 组装
        return new WnTianaiCaptchaApi(engine, conf, answerStore, ticketStore);
    }

    /**
     * @param name
     *            配置名
     * @return <code>~/.captcha/{name}.json</code> 的服务配置
     */
    private WnCaptchaConfig loadConfig(String name) {
        String ph = Wn.appendPath("~/.captcha", name + ".json5");
        // 常规化路径（~ -> 域主目录的绝对路径），IO 层只认标准绝对路径
        String aph = Wn.normalizePath(ph, vars);
        WnObj oConf = Wn.checkObj(io, aph);
        WnCaptchaConfig conf = io.readJson(oConf, WnCaptchaConfig.class);
        if (null == conf) {
            throw Er.create("e.captcha.BlankConfig", aph);
        }
        return conf;
    }

}
