package org.nutz.walnut.ext.wiki.hdl;

import org.nutz.lang.Streams;
import org.nutz.plugins.zdoc.NutDSet;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.wiki.WikiService;
import org.nutz.walnut.ext.wiki.WnHtmlDSetRender;
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
            try {
                wobj = sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys));
                NutDSet dset = new NutDSet(wobj.name());
                hc.ioc.get(WikiService.class).tree(wobj, dset, true);
                WnHtmlDSetRender render = new WnHtmlDSetRender();
                render.setIo(sys.io);
                render.setDst(sys.io.check(null, Wn.normalizeFullPath(dst, sys)));
                render.render(dset, dst);
            }
            catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
