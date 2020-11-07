package org.nutz.walnut.ext.truck.impl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.truck.TruckContext;

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
