package com.site0.walnut.ext.sys.truck.impl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.truck.TruckContext;

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
