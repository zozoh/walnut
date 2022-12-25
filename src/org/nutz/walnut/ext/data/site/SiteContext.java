package org.nutz.walnut.ext.data.site;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmFilterContext;

public class SiteContext extends JvmFilterContext {

    /**
     * 站点的原始路径，将根据这个路径下的内容进行站点发布
     */
    public WnObj oSite;
}
