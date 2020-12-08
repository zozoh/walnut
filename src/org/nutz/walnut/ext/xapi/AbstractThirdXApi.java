package org.nutz.walnut.ext.xapi;

import org.nutz.walnut.ext.xapi.bean.ThirdXRequest;
import org.nutz.walnut.ext.xapi.impl.ThirdXApiExpert;

public abstract class AbstractThirdXApi implements ThirdXApi {

    protected ThirdXAkManager accessKeyManager;

    protected ThirdXConfigLoader configLoader;
    
    protected ThirdXApiExpert expert;

    public ThirdXRequest prepare(String apiName, String path) {
        return new ThirdXRequest();
    }

    public ThirdXAkManager getAccessKeyManager() {
        return accessKeyManager;
    }

    public void setAccessKeyManager(ThirdXAkManager accessKeyManager) {
        this.accessKeyManager = accessKeyManager;
    }

    public ThirdXConfigLoader getConfigLoader() {
        return configLoader;
    }

    public void setConfigLoader(ThirdXConfigLoader configLoader) {
        this.configLoader = configLoader;
    }

}
