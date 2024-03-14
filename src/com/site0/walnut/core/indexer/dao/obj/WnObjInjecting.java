package com.site0.walnut.core.indexer.dao.obj;

import java.util.Map;

import org.nutz.lang.inject.Injecting;
import com.site0.walnut.api.err.Er;

public class WnObjInjecting implements Injecting {

    private String stdName;

    public WnObjInjecting(String stdName) {
        this.stdName = stdName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void inject(Object obj, Object value) {
        if (obj instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) obj;
            map.put(stdName, value);
            return;
        }
        throw Er.create("e.io.dao.entity.inject.NotSupportInjecting", obj.getClass().getName());
    }

}
