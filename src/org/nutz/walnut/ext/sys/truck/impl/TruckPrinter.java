package org.nutz.walnut.ext.sys.truck.impl;

import org.nutz.walnut.util.tmpl.WnTmpl;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnObj;

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
