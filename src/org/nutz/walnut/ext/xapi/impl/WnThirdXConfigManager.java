package org.nutz.walnut.ext.xapi.impl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.xapi.ThirdXApi;
import org.nutz.walnut.ext.xapi.ThirdXConfigManager;
import org.nutz.walnut.ext.xapi.ThirdXExpertManager;
import org.nutz.walnut.ext.xapi.bean.ThirdXAccessKey;
import org.nutz.walnut.ext.xapi.bean.ThirdXExpert;
import org.nutz.walnut.ext.xapi.bean.ThirdXRequest;
import org.nutz.walnut.util.Wn;

public class WnThirdXConfigManager implements ThirdXConfigManager {

    private ThirdXApi api;

    private ThirdXExpertManager experts;

    private WnIo io;

    private NutBean vars;

    private Map<String, NutMap> cacheConfig;

    private Map<String, ThirdXAccessKey> cacheAccessKey;

    public WnThirdXConfigManager(ThirdXApi api,
                                 ThirdXExpertManager experts,
                                 WnIo io,
                                 NutBean vars) {
        this.api = api;
        this.io = io;
        this.vars = vars;
        this.experts = experts;
        this.cacheConfig = new HashMap<>();
        this.cacheAccessKey = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadConfig(String apiName, String account, Class<T> configType) {
        ThirdXExpert expert = experts.checkExpert(apiName);
        String ph = Wn.appendPath(expert.getHome(), account, expert.getConfigFilePath());
        String aph = Wn.normalizeFullPath(ph, vars);

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
    public String loadAccessKey(String apiName, String account) {
        ThirdXExpert expert = experts.checkExpert(apiName);
        String ph = Wn.appendPath(expert.getHome(), account, expert.getAccessKeyFilePath());
        String aph = Wn.normalizeFullPath(ph, vars);

        // 先尝试命中缓存
        ThirdXAccessKey ak = cacheAccessKey.get(aph);
        if (null == ak || ak.isExpired() || !ak.hasTicket()) {
            synchronized (this) {
                // 再次看看缓存
                ak = cacheAccessKey.get(aph);

                // 还是木有，那么从持久化存储
                if (null == ak || ak.isExpired() || !ak.hasTicket()) {
                    WnObj oAk = io.fetch(null, aph);
                    if (null != oAk) {
                        ak = Lang.map2Object(oAk, ThirdXAccessKey.class);
                    }
                }

                // 还是木有，那么请求服务器
                if (null == ak || ak.isExpired() || !ak.hasTicket()) {
                    // 尝试请求一下服务器
                    NutMap config = this.loadConfig(apiName, account, NutMap.class);
                    ThirdXRequest req = expert.getAccessKeyRequest();
                    req.explainHeaders(config);
                    req.explainParams(config);
                    NutMap re = api.send(req, NutMap.class);

                    // 转换成标准的请求对象
                    NutMap ao = (NutMap) Wn.explainObj(re, expert.getAccessKeyObj());
                    ak = Lang.map2Object(ao, ThirdXAccessKey.class);

                    // 初始化一下绝对过期时间，其中稍微少设 30 秒，保险一点
                    ak.setNowInMs(System.currentTimeMillis() - 30000L);

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
