package org.nutz.walnut.core.indexer.dao;

import java.util.Map;

import org.nutz.dao.entity.MappingField;
import org.nutz.lang.eject.Ejecting;
import org.nutz.walnut.api.err.Er;

public class WnObjEjecting implements Ejecting {

    private MappingField fld;

    public WnObjEjecting(MappingField fld) {
        this.fld = fld;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object eject(Object obj) {
        if (obj instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) obj;
            return map.get(fld.getName());
        }
        throw Er.create("e.io.dao.entity.eject.NotSupportEjecting", obj.getClass().getName());
    }

}
