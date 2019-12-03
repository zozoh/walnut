package org.nutz.walnut.ext.trash.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;

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
