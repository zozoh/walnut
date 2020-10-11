package org.nutz.walnut.ext.titanium.www;

import java.util.HashMap;
import java.util.Map;

public class WebsiteState {

    private HashMap<String, WebsiteApi> apis;

    public WebsiteApi getApi(String apiName) {
        if (null == apis)
            return null;
        return apis.get(apiName);
    }

    public HashMap<String, WebsiteApi> getApis() {
        return apis;
    }

    public void setApis(HashMap<String, WebsiteApi> apis) {
        this.apis = apis;
    }

    public Map<String, WebsiteApi> getPageSSRApi() {
        Map<String, WebsiteApi> map = new HashMap<>();

        for (Map.Entry<String, WebsiteApi> en : apis.entrySet()) {
            WebsiteApi api = en.getValue();
            if (api.isPages() && api.isSsr()) {
                map.put(en.getKey(), api);
            }
        }

        return map;
    }

}
