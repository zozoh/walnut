package com.site0.walnut.ext.sys.trash.hdl;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;

public abstract class trash_xxx implements JvmHdl {

    protected WnObj checkUserTrashRoot(WnSystem sys) {
        final WnObj[] re = new WnObj[1];
        WnContext wc = Wn.WC();
        wc.nosecurity(sys.io, () -> {
            re[0] = sys.io.createIfNoExists(null, "/trash/" + sys.getMyName(), WnRace.DIR);
        });
        return re[0];
    }
}
