package org.nutz.walnut.ext.sys.refer.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoBM;
import org.nutz.walnut.core.WnIoMapping;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class refer_add implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String ph = hc.params.val_check(0);
        WnObj o = Wn.checkObj(sys, ph);

        String sha1 = hc.params.val_check(1);

        // 防守
        if (Wn.Io.isEmptySha1(sha1)) {
            return;
        }

        WnIoMapping im = sys.io.getMappingFactory().checkMapping(o);
        WnIoBM bm = im.getBucketManager();
        
        

    }

}
