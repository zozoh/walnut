package org.nutz.walnut.ext.truck.impl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.truck.TruckContext;

class EachTruckIndexer extends EachTruck {

    EachTruckIndexer(TruckContext tc) {
        super(tc);
    }

    @Override
    public void invoke(int index, WnObj obj, int length) {
        WnObj taObj = this.transIndexer(obj);
        this.appendObj(taObj);
    }

}
