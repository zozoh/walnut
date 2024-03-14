package com.site0.walnut.ext.data.site;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmFilterContext;

public class SiteContext extends JvmFilterContext {

    /**
     * 站点的原始路径，将根据这个路径下的内容进行站点发布
     */
    public WnObj oSite;
}
