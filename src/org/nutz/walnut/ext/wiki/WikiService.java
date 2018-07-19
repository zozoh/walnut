package org.nutz.walnut.ext.wiki;

import java.io.Reader;
import java.io.Writer;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;

@IocBean
public class WikiService {

    @Inject
    protected WnIo io;
    
    public WikiNode tree(WnObj root, NutMap confs) {
        // if root is dir, search for [tree.xml, tree.md, tree.json]
        if (root.isDIR()) {
            WnObj tmp = io.fetch(root, "tree.xml");
            if (tmp == null) {
                tmp = io.fetch(root, "tree.md");
            }
            if (tmp == null) {
                tmp = io.fetch(root, "tree.json");
            }
            // none of  [tree.xml, tree.md, tree.json] found, use struct of dir
            if (tmp == null) {
                
            }
        }
        return null;
    }
    
    public void render(Reader r, Writer w, NutMap confs) {
        
    }
}
