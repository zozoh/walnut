package org.nutz.walnut.ext.wiki.hdl;

import org.nutz.lang.Streams;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class wiki_render implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String base = hc.params.get("base");
        String dst = hc.params.get("dst");
        String linkbase = hc.params.get("linkbase");
        String tmpl = hc.params.get("tmpl");
        String tree = hc.params.get("tree");
        String treeName = hc.params.get("treeName");
        
        String pipeStr = null;
        WnObj wobj = null;
        if (sys.pipeId > 0) {
            pipeStr = Streams.readAndClose(sys.in.getReader());
        }
        else {
            wobj = sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys));
        }
    }

}
