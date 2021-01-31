package org.nutz.walnut.core.indexer.dao;

import org.nutz.dao.impl.entity.NutEntity;
import org.nutz.walnut.core.bean.WnIoObj;

public class WnObjEntity extends NutEntity<WnIoObj> {

    public WnObjEntity() {
        super(WnIoObj.class);
    }

    public boolean hasField(String name) {
        return null != this.getField(name);
    }

    public boolean hasNameField() {
        return hasField("nm");
    }
}
