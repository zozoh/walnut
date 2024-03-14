package com.site0.walnut.core;

import java.io.File;

import org.nutz.ioc.impl.PropertiesProxy;
import org.nutz.json.Json;
import org.nutz.lang.Files;
import com.site0.walnut.api.io.MimeMap;
import com.site0.walnut.api.io.WnIoIndexer;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.indexer.dao.DaoIndexer;
import com.site0.walnut.core.mapping.WnIndexerFactory;
import com.site0.walnut.ext.sys.sql.WnDaoAuth;
import com.site0.walnut.ext.sys.sql.WnDaoMappingConfig;

public class MockDaoIndexerFactory implements WnIndexerFactory {

    private PropertiesProxy unitSetup;

    private MimeMap mimes;

    private WnDaoAuth auth;

    @Override
    public WnIoIndexer load(WnObj oHome, String str) {
        // 加载配置
        WnDaoMappingConfig conf = loadConfig(str);

        return new DaoIndexer(oHome, mimes, conf);
    }

    public WnDaoMappingConfig loadConfig(String str) {
        File f = Files.findFile("com/site0/walnut/api/dao/ix_" + str + ".json");
        String text = Files.read(f);
        WnDaoMappingConfig conf = Json.fromJson(WnDaoMappingConfig.class, text);
        conf.setAuth(auth);
        return conf;
    }

    public PropertiesProxy getUnitSetup() {
        return unitSetup;
    }

    public WnDaoAuth getAuth() {
        return auth;
    }

    public void setAuth(WnDaoAuth auth) {
        this.auth = auth;
    }

    public void setUnitSetup(PropertiesProxy unitSetup) {
        this.unitSetup = unitSetup;
        auth = new WnDaoAuth();
        auth.setUrl(unitSetup.get("jdbc-url"));
        auth.setUsername(unitSetup.get("jdbc-username"));
        auth.setPassword(unitSetup.get("jdbc-password"));
        auth.setMaxWait(15000);
        auth.setMaxActive(50);
        auth.setTestWhileIdle(true);
    }

    public MimeMap getMimes() {
        return mimes;
    }

    public void setMimes(MimeMap mimes) {
        this.mimes = mimes;
    }

}
