package org.nutz.walnut.ext.truck.hdl;

import org.nutz.walnut.ext.truck.TruckContext;
import org.nutz.walnut.ext.truck.TruckFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class truck_buf extends TruckFilter {

    @Override
    protected void process(WnSystem sys, TruckContext fc, ZParams params) {
        fc.bufferSize = params.val_check_int(0);
    }

}