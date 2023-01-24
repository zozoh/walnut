package org.nutz.walnut.ext.net.xapi.impl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.net.xapi.XApi;
import org.nutz.walnut.ext.net.xapi.XApiCacheObj;
import org.nutz.walnut.ext.net.xapi.XApiConfigManager;
import org.nutz.walnut.ext.net.xapi.XApiExpertManager;
import org.nutz.walnut.ext.net.xapi.bean.XApiAccessKey;
import org.nutz.walnut.ext.net.xapi.bean.XApiExpert;
import org.nutz.walnut.ext.net.xapi.bean.XApiRequest;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

public class WnXApiConfigManager implements XApiConfigManager {

    private XApi api;

    private XApiExpertManager experts;

    private WnIo io;

    private NutBean vars;

    private Map<String, NutMap> cacheConfig;

    private Map<String, XApiAccessKey> cacheAccessKey;

    public WnXApiConfigManager(XApi api, XApiExpertManager experts, WnIo io, NutBean vars) {
        this.api = api;
        this.io = io;
        this.vars = vars;
        this.experts = experts;
        this.cacheConfig = new HashMap<>();
        this.cacheAccessKey = new HashMap<>();
    }

    @Override
    public XApiCacheObj loadReqCache(XApiRequest req) {
        if (!req.isCacheEnabled()) {
            return new NilXapiCacheObj(req);
        }

        String apiName = req.getApiName();
        String account = req.getAccount();
        XApiExpert expert = experts.checkExpert(apiName);
        String cachePath = expert.getApiCachePath();
        if (Ws.isBlank(cachePath)) {
            return new NilXapiCacheObj(req);
        }
        // 准备缓存目录
        String aph = expert.getPathInHome(account, cachePath, vars);
        WnObj oCacheHome = io.createIfNoExists(null, aph, WnRace.DIR);

        return new WnXapiCacheObj(io, oCacheHome, req);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadConfig(String apiName, String account, Class<T> configType) {
        XApiExpert expert = experts.checkExpert(apiName);
        String confPath = expert.getConfigFilePath();
        String aph = expert.getPathInHome(account, confPath, vars);

        // 先尝试命中缓存
        NutMap config = cacheConfig.get(aph);
        if (null == config) {
            synchronized (this) {
                config = cacheConfig.get(aph);
                if (null == config) {
                    WnObj oConfig = io.check(null, aph);
                    config = io.readJson(oConfig, NutMap.class);
                    cacheConfig.put(aph, config);
                }
            }
        }

        // 搞定配置项
        if (configType.isAssignableFrom(Map.class)) {
            return (T) config;
        }

        // 转换到需要的类型
        return Lang.map2Object(config, configType);
    }

    @Override
    public NutMap loadConfig(String apiName, String account) {
        return loadConfig(apiName, account, NutMap.class);
    }

    public boolean hasValidAccessKey(String apiName, String account) {
        XApiExpert expert = experts.checkExpert(apiName);

        // 无需动态密钥，那么永远是 true 咯
        if (!expert.isDynamicAccessKey()) {
            return true;
        }

        String ph = Wn.appendPath(expert.getHome(), account, expert.getAccessKeyFilePath());
        String aph = Wn.normalizeFullPath(ph, vars);

        XApiAccessKey ak = cacheAccessKey.get(aph);
        if (null == ak || ak.isExpired() || !ak.hasTicket()) {
            synchronized (this) {
                ak = cacheAccessKey.get(aph);
                // 还是木有，那么从持久化存储
                if (null == ak || ak.isExpired() || !ak.hasTicket()) {
                    WnObj oAk = io.fetch(null, aph);
                    if (null != oAk) {
                        ak = Lang.map2Object(oAk, XApiAccessKey.class);
                        return null != ak && !ak.isExpired() && ak.hasTicket();
                    }
                }
            }
        }

        return false;
    }

    @Override
    public String loadAccessKey(String apiName, String account, NutBean vars, boolean force) {
        XApiExpert expert = experts.checkExpert(apiName);
        String akPath = expert.getAccessKeyFilePath();
        String aph = expert.getPathInHome(account, akPath, vars);
        // 如果强制，那么先移除缓存
        if (force) {
            cacheAccessKey.remove(aph);
        }

        // 先尝试命中缓存
        XApiAccessKey ak = cacheAccessKey.get(aph);
        if (null == ak || ak.isExpired() || !ak.hasTicket()) {
            synchronized (this) {
                // 再次看看缓存
                if (!force) {
                    ak = cacheAccessKey.get(aph);
                }

                // 还是木有，那么从持久化存储
                if (!force && (null == ak || ak.isExpired() || !ak.hasTicket())) {
                    WnObj oAk = io.fetch(null, aph);
                    if (null != oAk) {
                        ak = Lang.map2Object(oAk, XApiAccessKey.class);
                    }
                }

                // 虽然读取到了（当然，也可能是没有读取到），我们还应该看看这个密钥是否有效以及是否过期
                // 如果木有，那么需要重新获取（生成）一下密钥
                if (null == ak || ak.isExpired() || !ak.hasTicket()) {
                    // 首先要获取一下配置信息
                    NutMap config = this.loadConfig(apiName, account);

                    // 准备一个上下文，这个 config 可能从缓存来，所以要复制一下
                    NutMap context = config.duplicate();
                    if (null != vars)
                        context.putAll(vars);

                    // 动态密钥，需要请求服务器
                    if (expert.isDynamicAccessKey()) {
                        XApiRequest req = expert.getAccessKeyRequest().clone();
                        req.setApiName(apiName);
                        req.setAccount(account);
                        req.expalinPath(context);
                        req.explainHeaders(context);
                        req.explainParams(context);
                        NutMap re = api.send(req, NutMap.class);

                        // 转换成标准的请求对象
                        NutMap ao = (NutMap) Wn.explainObj(re, expert.getAccessKeyObj());
                        ak = Lang.map2Object(ao, XApiAccessKey.class);
                    }
                    // 否则就是模板密钥
                    else {
                        NutMap ao = (NutMap) Wn.explainObj(context, expert.getAccessKeyObj());
                        ak = Lang.map2Object(ao, XApiAccessKey.class);
                    }

                    // 初始化一下绝对过期时间，其中稍微少设 300秒，保险一点
                    ak.setNowInMs(System.currentTimeMillis() - 300000L);

                    // 计入缓存
                    cacheAccessKey.put(aph, ak);

                    // 持久化
                    WnObj oAk = io.createIfNoExists(null, aph, WnRace.FILE);
                    NutBean meta = ak.toBean();
                    io.appendMeta(oAk, meta);
                }
            }
        }

        // 搞定密钥
        return ak.getTicket();
    }

}
