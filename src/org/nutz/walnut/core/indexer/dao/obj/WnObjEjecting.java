package org.nutz.walnut.core.indexer.dao.obj;

import java.util.Map;

import org.nutz.lang.eject.Ejecting;
import org.nutz.walnut.api.err.Er;

public class WnObjEjecting implements Ejecting {

    private String stdName;

    public WnObjEjecting(String stdName) {
        this.stdName = stdName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object eject(Object obj) {
        if (obj instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) obj;
            return map.get(stdName);
        }
        throw Er.create("e.io.dao.entity.eject.NotSupportEjecting", obj.getClass().getName());
    }

}
