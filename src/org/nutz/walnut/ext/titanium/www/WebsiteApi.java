package org.nutz.walnut.ext.titanium.www;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wn;

public class WebsiteApi {

    private String title;

    private String path;

    private WebsiteApiMethod method;

    private NutMap params;

    private String dataKey;

    private int preload;

    private boolean pages;

    private boolean ssr;

    public WebsiteApi() {
        this.preload = -1;
    }

    public String toString() {
        return String.format("[%d]%s:%s{}=>%s", preload, method, path, dataKey);
    }

    public WebsiteApi clone() {
        WebsiteApi api = new WebsiteApi();
        api.title = this.title;
        api.path = this.path;
        api.method = this.method;
        if (null != this.params) {
            String json = Json.toJson(this.params, JsonFormat.compact().setIgnoreNull(false));
            api.params = Json.fromJson(NutMap.class, json);
        }
        api.dataKey = this.dataKey;
        api.preload = this.preload;
        api.pages = this.pages;
        return api;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public WebsiteApiMethod getMethod() {
        return method;
    }

    public void setMethod(WebsiteApiMethod method) {
        this.method = method;
    }

    public boolean hasParams() {
        return null != params && params.size() > 0;
    }

    public Object getParam(String name) {
        return params.get(name);
    }

    public NutMap getParams() {
        return params;
    }

    public void setParams(NutMap params) {
        this.params = params;
    }

    public void explainParams(NutBean context) {
        // 确保有上下文
        if (null == context) {
            context = new NutMap();
        }
        NutMap p2 = (NutMap) Wn.explainObj(context, this.params);
        this.params = p2;
    }

    public String getParamsValueJson(JsonFormat jfmt) {
        if (null == jfmt) {
            jfmt = JsonFormat.compact().setQuoteName(true).setIgnoreNull(false);
        }
        NutMap map = this.getParams();
        return Json.toJson(map, jfmt);
    }

    public String getParamsValueJson() {
        return this.getParamsValueJson(null);
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
    }

    public boolean isPreload() {
        return this.preload >= 0;
    }

    public int getPreload() {
        return preload;
    }

    public void setPreload(int preload) {
        this.preload = preload;
    }

    public boolean isPages() {
        return pages;
    }

    public void setPages(boolean pages) {
        this.pages = pages;
    }

    public boolean isSsr() {
        return ssr;
    }

    public void setSsr(boolean ssr) {
        this.ssr = ssr;
    }

}
