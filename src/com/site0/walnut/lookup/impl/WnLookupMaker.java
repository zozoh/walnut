package com.site0.walnut.lookup.impl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.log.Log;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.lookup.WnLookup;
import com.site0.walnut.lookup.WnTestLookup;
import com.site0.walnut.lookup.bean.LookupConfig;
import com.site0.walnut.lookup.bean.LookupType;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;

public class WnLookupMaker {

    private static Log log = Wlog.getCMD();

    private WnIo io;

    private Map<String, WnLookup> lookups;

    public WnLookupMaker(WnIo io) {
        this.io = io;
        this.lookups = new HashMap<>();
    }

    public void clear() {
        this.lookups.clear();
    }

    public WnLookup getLookup(String name) {
        // 特殊名称: test
        if ("test".equals(name)) {
            return new WnTestLookup();
        }
        // 名称必须为 ${domainName}/${lookupName}
        // 譬如 name="demo/pets"
        // 则将会从 `/home/demo/.lookup/pets.lookup.json`
        // 处获取配置
        if (log.isInfoEnabled()) {
            log.infof("getLookup: '%s'", name);
        }
        WnLookup lookup = lookups.get(name);
        if (null == lookup) {
            synchronized (this) {
                lookup = lookups.get(name);
                if (null == lookup) {
                    LookupConfig config = createConfig(name);
                    if (LookupType.SQL == config.getType()) {
                        lookup = new WnSqlLookup(io, config);
                    }
                    // 不支持的类型
                    else {
                        throw Er.create("e.lookup.type.Invalid", Json.toJson(config));
                    }
                    // 记入缓存
                    lookups.put(name, lookup);
                }
            }
        }

        // 搞定
        return lookup;
    }

    private LookupConfig createConfig(String name) {
        String[] ss = Ws.splitIgnoreBlank(name, "[:/>]");
        if (ss.length != 2) {
            throw Er.create("e.lookup.name.Invalid", name);
        }
        String domainName = ss[0];
        String lookupName = ss[1];
        String path = "/home/" + domainName + "/.lookup/" + lookupName + ".lookup.json";
        if (log.isInfoEnabled()) {
            log.infof("createLookup: '%s' => %s", name, path);
        }
        WnObj oConfig = io.check(null, path);
        String json = io.readText(oConfig);
        LookupConfig config = Json.fromJson(LookupConfig.class, json);
        return config;
    }

}
