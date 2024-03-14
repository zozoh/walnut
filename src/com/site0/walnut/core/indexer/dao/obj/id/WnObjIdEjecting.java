package com.site0.walnut.core.indexer.dao.obj.id;

import com.site0.walnut.core.bean.WnObjId;
import com.site0.walnut.core.indexer.dao.obj.WnObjEjecting;

public class WnObjIdEjecting extends WnObjEjecting {

    public WnObjIdEjecting(String stdName) {
        super(stdName);
    }

    @Override
    public Object eject(Object obj) {
        Object val = super.eject(obj);

        if (null == val) {
            return null;
        }
        // 如果是两段式 ID，则采用后段
        WnObjId oid = new WnObjId(val.toString());
        return oid.getMyId();
    }
}
