package com.site0.walnut.core.indexer.dao.obj.id;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.indexer.dao.obj.WnObjInjecting;

public class WnObjIdInjecting extends WnObjInjecting {

    private WnObj root;

    public WnObjIdInjecting(String stdName, WnObj root) {
        super(stdName);
        this.root = root;
    }

    @Override
    public void inject(Object obj, Object value) {
        // 如果声明了根对象，则采用两段式 ID
        if (null != root && null != value) {
            String rootId = root.myId();
            if (null != rootId && !rootId.startsWith("@")) {
                value = root.myId() + ":" + value;
            }
        }
        // 注入
        super.inject(obj, value);
    }
}
