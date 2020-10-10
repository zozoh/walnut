package org.nutz.walnut.ext.titanium.www;

import org.nutz.lang.util.NutMap;

public class WebsiteApiDeclare extends WebsiteApi {

    private String apiName;

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    /**
     * @param api
     *            站点全局的 api 定义
     * @return 于自己参数合并后的 api 对象
     */
    public WebsiteApi mergeToApi(WebsiteApi api) {
        api = api.clone();
        // 合并参数
        if (api.hasParams() && this.hasParams()) {
            for (String name : api.getParams().keySet()) {
                // 全局定义 api 的参数
                NutMap param = api.getParamDefine(name);

                // 看看自己有木有特殊定义
                Object val = this.getParam(name);
                if (null != val) {
                    param.put("value", val);
                }

            }
        }
        // 返回
        return api;
    }

}
