package org.nutz.walnut.ext.dom.hdl;

import org.nutz.walnut.ext.dom.DomContext;
import org.nutz.walnut.ext.dom.DomFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class dom_heading extends DomFilter {

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        fc.current = fc.doc.body().findElement(el -> {
            return el.isTagAs("^H[1-6]$");
        });
    }

}