package org.nutz.walnut.ext.sys.truck.impl;

import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnObj;

public class TruckPrinter {

    private Tmpl tmpl;

    private WnOutputable out;

    public TruckPrinter(WnOutputable output, Tmpl tmpl) {
        this.tmpl = tmpl;
        this.out = output;
    }

    public void print(WnObj o) {
        String str = tmpl.render(o);
        out.println(str);
    }

}
