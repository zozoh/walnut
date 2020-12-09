package org.nutz.walnut.ext.xapi.bean;

import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;

public class ThirdXExpert {

    private String name;

    private String base;

    private String home;

    private String configFilePath;

    private String accessKeyFilePath;

    private ThirdXRequest accessKeyRequest;

    private NutMap accessKeyObj;

    private Map<String, ThirdXRequest> requests;

    public ThirdXExpert() {
        accessKeyFilePath = "access_key";
        requests = new HashMap<>();
    }

    public ThirdXRequest get(String key) {
        ThirdXRequest req = this.requests.get(key);
        if (null != req) {
            req = req.clone();
            req.setApiName(this.name);
            req.setBase(this.base);
            return req;
        }
        return null;
    }

    public ThirdXRequest check(String key) {
        ThirdXRequest req = this.get(key);
        if (null == req) {
            throw Er.createf("e.ThirdXApi.InvalidApiKey", "[%s] %s : %s", name, base, key);
        }
        return req;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public String getAccessKeyFilePath() {
        return accessKeyFilePath;
    }

    public void setAccessKeyFilePath(String accessKeyFileName) {
        this.accessKeyFilePath = accessKeyFileName;
    }

    public ThirdXRequest getAccessKeyRequest() {
        ThirdXRequest akr = accessKeyRequest.clone();
        akr.setApiName(name);
        akr.setBase(this.base);
        return akr;
    }

    public void setAccessKeyRequest(ThirdXRequest accessKey) {
        this.accessKeyRequest = accessKey;
    }

    public NutMap getAccessKeyObj() {
        return accessKeyObj;
    }

    public void setAccessKeyObj(NutMap accessKeyObj) {
        this.accessKeyObj = accessKeyObj;
    }

    public Map<String, ThirdXRequest> getRequests() {
        return requests;
    }

    public void setRequests(Map<String, ThirdXRequest> requests) {
        this.requests = requests;
    }

}
