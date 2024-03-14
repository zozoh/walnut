package com.site0.walnut.ext.media.dom.hdl;

import com.site0.walnut.ext.media.dom.DomContext;
import com.site0.walnut.ext.media.dom.DomFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class dom_format extends DomFilter {

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        fc.doc.formatAsHtml();
    }

}
