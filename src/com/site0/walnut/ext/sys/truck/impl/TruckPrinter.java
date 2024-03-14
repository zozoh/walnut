package com.site0.walnut.ext.sys.truck.impl;

import com.site0.walnut.util.tmpl.WnTmpl;
import com.site0.walnut.api.WnOutputable;
import com.site0.walnut.api.io.WnObj;

public class TruckPrinter {

    private WnTmpl tmpl;

    private WnOutputable out;

    public TruckPrinter(WnOutputable output, WnTmpl tmpl) {
        this.tmpl = tmpl;
        this.out = output;
    }

    public void print(WnObj o) {
        String str = tmpl.render(o);
        out.println(str);
    }

}
