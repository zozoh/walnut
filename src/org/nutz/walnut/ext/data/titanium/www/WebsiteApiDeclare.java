package org.nutz.walnut.ext.data.titanium.www;

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
            api.getParams().putAll(this.getParams());
        }
        // 返回
        return api;
    }

}
