package org.nutz.walnut.ext.dom.hdl;

import java.io.OutputStream;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.cheap.dom.docx.CheapDocxRendering;
import org.nutz.walnut.ext.dom.DomContext;
import org.nutz.walnut.ext.dom.DomFilter;
import org.nutz.walnut.ext.dom.docx.WnDocxResourceLoader;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class dom_docx extends DomFilter {

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        OutputStream out = null;
        // 打开输出文件
        String outPath = params.getString("out");
        if (Strings.isBlank(outPath)) {
            out = sys.out.getOutputStream();
        } else {
            String aph = Wn.normalizeFullPath(outPath, sys);
            WnObj oOut = sys.io.createIfNoExists(null, aph, WnRace.FILE);
            out = sys.io.getOutputStream(oOut, 0);
        }
        try {
            WordprocessingMLPackage wp = WordprocessingMLPackage.createPackage();
            WnDocxResourceLoader loader = new WnDocxResourceLoader(sys);
            CheapDocxRendering ing = new CheapDocxRendering(fc.doc, wp, null, loader);
            ing.render();
            wp.save(out);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw Er.wrap(e);
        }
        finally {
            Streams.safeClose(out);
        }

    }

}
