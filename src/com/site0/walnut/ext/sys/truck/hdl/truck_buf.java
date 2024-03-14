package com.site0.walnut.ext.sys.truck.hdl;

import com.site0.walnut.ext.sys.truck.TruckContext;
import com.site0.walnut.ext.sys.truck.TruckFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class truck_buf extends TruckFilter {

    @Override
    protected void process(WnSystem sys, TruckContext fc, ZParams params) {
        fc.bufferSize = params.val_check_int(0);
    }

}
