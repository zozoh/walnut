package org.nutz.walnut.core.indexer.dao;

import java.util.Map;

import org.nutz.dao.entity.MappingField;
import org.nutz.lang.inject.Injecting;
import org.nutz.walnut.api.err.Er;

public class WnObjInjecting implements Injecting {

    private MappingField fld;

    public WnObjInjecting(MappingField fld) {
        this.fld = fld;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void inject(Object obj, Object value) {
        if (obj instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) obj;
            map.put(fld.getName(), value);
            return;
        }
        throw Er.create("e.io.dao.entity.inject.NotSupportInjecting", obj.getClass().getName());
    }

}
