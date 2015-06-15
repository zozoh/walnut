package org.nutz.walnut.ext.site;

import org.nutz.walnut.impl.box.WnSystem;

public interface SiteHdl {

    void invoke(WnSystem sys, ShCtx sc) throws Exception;

}
