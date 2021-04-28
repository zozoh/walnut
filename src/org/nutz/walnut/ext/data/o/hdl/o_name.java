package org.nutz.walnut.ext.data.o.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wpath;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class o_name extends OFilter {

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        String key = params.val(0, "name");

        for (WnObj o : fc.list) {
            String nm = o.name();
            String majorName = Wpath.getMajorName(nm);
            if (!Ws.isBlank(majorName)) {
                o.put(key, majorName);
            }
        }
    }

}
