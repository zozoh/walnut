package org.nutz.walnut.ext.data.site.render;

import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnObj;

public class SitePageRendering {

    private WnObj siteHome;

    private SitePageRenderConfig config;

    protected WnOutputable out;
    
    

    public WnObj getSiteHome() {
        return siteHome;
    }

    public void setSiteHome(WnObj siteHome) {
        this.siteHome = siteHome;
    }

    public SitePageRenderConfig getConfig() {
        return config;
    }

    public void setConfig(SitePageRenderConfig config) {
        this.config = config;
    }

    public WnOutputable getOut() {
        return out;
    }

    public void setOut(WnOutputable out) {
        this.out = out;
    }

}
