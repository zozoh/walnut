package org.nutz.walnut.ext.mediax.impl;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.born.Borning;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.resource.Scans;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.mediax.MediaXAPI;
import org.nutz.walnut.ext.mediax.MediaXService;
import org.nutz.walnut.ext.mediax.MxAPIKey;
import org.nutz.walnut.ext.mediax.apis.AbstractMediaXAPI;
import org.nutz.walnut.ext.mediax.bean.MxAccount;

/**
 * 作为所有 MeidaXService 的父类。主要提供一些便捷的 MediaXAPI 的创建帮助函数等
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractMediaXService implements MediaXService {

    private static final Log log = Logs.get();

    private Map<String, Borning<MediaXAPI>> map;

    /**
     * 在构造函数
     */
    @SuppressWarnings("unchecked")
    public AbstractMediaXService() {
        if (log.isInfoEnabled()) {
            log.info("Init MediaXService");
        }
        map = new HashMap<>();

        // 扫描本包下的所有类
        List<Class<?>> list = Scans.me().scanPackage(AbstractMediaXAPI.class);
        for (Class<?> klass : list) {
            // 跳过抽象类
            int mod = klass.getModifiers();
            if (Modifier.isAbstract(mod))
                continue;

            // 跳过非公共的类
            if (!Modifier.isPublic(klass.getModifiers()))
                continue;

            // 跳过内部类
            if (klass.getName().contains("$"))
                continue;

            // 必须保证声明了注解
            MxAPIKey host = klass.getAnnotation(MxAPIKey.class);
            if (null == host)
                continue;

            // 如果是 HopeHdl 的实现类 ...
            Mirror<?> mi = Mirror.me(klass);
            if (mi.isOf(MediaXAPI.class)) {
                Class<MediaXAPI> k2 = (Class<MediaXAPI>) klass;
                Borning<MediaXAPI> borning = Mirror.me(k2).getBorningByArgTypes(MxAccount.class);
                map.put(host.value(), borning);
                if (log.isInfoEnabled()) {
                    log.infof(" + %s -> %s", host, klass.getSimpleName());
                }
            }

            if (log.isInfoEnabled()) {
                log.infof("%d API founded", map.size());
            }
        }
    }

    @Override
    public MediaXAPI create(URI uri, String account) {
        // 必须要有 URI 啊
        if (null == uri)
            throw Er.create("e.mediax.NullURI");

        // 根据 uri 得到 apiKey，如果是http/https 协议看 host
        String pro = uri.getScheme();
        String apiKey;
        if (pro.matches("^https?$")) {
            apiKey = uri.getHost();
        }
        // 否则用协议名
        else {
            apiKey = uri.getScheme();
        }

        // 得到平台的账号文件
        MxAccount ac = null;
        if (!Strings.isBlank(account) && !Strings.isBlank(apiKey)) {
            ac = _gen_account(apiKey, account);
        }

        // 得到创建器
        Borning<MediaXAPI> borning = this.map.get(apiKey);
        if (null == borning) {
            throw Er.create("e.mediax.UnsupportApiKey", apiKey);
        }

        // 来，创建吧
        return borning.born(ac);
    }

    protected abstract MxAccount _gen_account(String apiKey, String account);

}
