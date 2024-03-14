package com.site0.walnut.ext.sys.truck.impl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.truck.TruckContext;

class EachTruckBM extends EachTruck {

    EachTruckBM(TruckContext tc) {
        super(tc);
    }

    @Override
    public void invoke(int index, WnObj obj, int length) {
        this.transBM(obj, null);
        this.appendObj(obj);
    }

}
