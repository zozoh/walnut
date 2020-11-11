package org.nutz.walnut.ext.app.impl;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.app.bean.init.AppInitItem;

public class AppInitItemContext extends AppInitContext {

    public AppInitItem item;

    public WnObj checkObj(WnRace race) {
        return io.createIfNoExists(oDist, item.getPath(), race);
    }

    public NutMap genMeta(boolean includeProperties) {
        NutMap meta = new NutMap();
        meta.putAll(item.getMeta());
        if (includeProperties) {
            meta.putAll(item.getProperties());
        }
        if (item.hasLinkPath()) {
            meta.put("ln", item.getLinkPath());
        }
        return meta;
    }

    public void writeFile(WnObj obj) {
        throw Lang.noImplement();
    }

}
