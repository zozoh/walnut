package com.site0.walnut.ext.data.o.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.o.OContext;
import com.site0.walnut.ext.data.o.OFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wpath;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

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
