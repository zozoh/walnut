package org.nutz.walnut.ext.titanium.www;

import org.nutz.lang.util.NutMap;

public class WebsiteApi {

    private String title;

    private String path;

    private WebsiteApiMethod method;

    private NutMap params;

    private String dataKey;

    private int preload;

    private boolean pages;

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

    public NutMap getParams() {
        return params;
    }

    public void setParams(NutMap params) {
        this.params = params;
    }

    public String getDataKey() {
        return dataKey;
    }

    public void setDataKey(String dataKey) {
        this.dataKey = dataKey;
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

}
