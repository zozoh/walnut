package org.nutz.walnut.ext.dom.hdl;

import java.io.File;
import java.io.OutputStream;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.cheap.dom.docx.CheapDocxRendering;
import org.nutz.walnut.ext.dom.DomContext;
import org.nutz.walnut.ext.dom.DomFilter;
import org.nutz.walnut.ext.dom.docx.WnDocxResourceLoader;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class dom_docx extends DomFilter {

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        File f = Files.createFileIfNoExists2("D:/tmp/docx/output.docx");
        OutputStream out = Streams.fileOut(f);
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
