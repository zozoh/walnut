package org.nutz.walnut.ext.sys.truck.impl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.sys.truck.TruckContext;

public class EachTruckBoth extends EachTruck {

    EachTruckBoth(TruckContext tc) {
        super(tc);
    }

    @Override
    public void invoke(int index, WnObj obj, int length) {
        WnObj taObj = this.transIndexer(obj);
        this.transBM(obj, taObj);
        this.appendObj(taObj);
    }

}
