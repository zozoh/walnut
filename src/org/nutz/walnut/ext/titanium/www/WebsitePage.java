package org.nutz.walnut.ext.titanium.www;

import java.util.HashMap;
import java.util.Map;

public class WebsitePage {

    private HashMap<String, WebsiteApiDeclare> apis;

    public HashMap<String, WebsiteApiDeclare> getApis() {
        return apis;
    }

    public void setApis(HashMap<String, WebsiteApiDeclare> apis) {
        this.apis = apis;
    }

    public Map<String, WebsiteApi> getPreloaSsrdApi(WebsiteState site) {
        Map<String, WebsiteApi> map = new HashMap<>();

        // 首先看看 site 里有哪些 api 是一定要加入 page 里的
        map.putAll(site.getPageSSRApi());

        // 然后寻找自己的 api
        for (Map.Entry<String, WebsiteApiDeclare> en : apis.entrySet()) {
            WebsiteApiDeclare api = en.getValue();

            // 不是服务器渲染的预先加载 api
            if (!api.isSsr() || !api.isPreload())
                continue;

            // 得到站点 api
            WebsiteApi siteApi = site.getApi(api.getApiName());
            if (null != siteApi) {
                WebsiteApi api2 = api.mergeToApi(siteApi);

                // 计入
                map.put(en.getKey(), api2);
            }
        }

        return map;
    }
}
