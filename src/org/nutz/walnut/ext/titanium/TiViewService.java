package org.nutz.walnut.ext.titanium;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.util.TiView;
import org.nutz.walnut.ext.titanium.util.TiViewMapping;

@IocBean
public class TiViewService {

    @Inject("refer:io")
    private WnIo io;

    private WnObjCachedFactory<TiViewMapping> mappings;

    public TiViewService() {
        mappings = new WnObjCachedFactory<>();
    }

    public TiViewMapping getMapping(WnObj oMapping) {
        if (null == oMapping)
            return null;

        TiViewMapping mapping = mappings.get(oMapping);
        synchronized (this) {
            mapping = mappings.get(oMapping);
            if (null == mapping) {
                mapping = io.readJson(oMapping, TiViewMapping.class);
                if (null != mapping) {
                    mapping.init(io, oMapping);
                    mappings.set(oMapping, mapping);
                }
            }
        }

        return mapping;
    }
    
    public TiView getView(WnObj oMapping, WnObj o) {
        TiViewMapping mapping = this.getMapping(oMapping);
        if(null!=mapping) {
            return mapping.match(o);
        }
        return null;
    }

}
