package org.nutz.walnut.ext.media.dom.hdl;

import org.nutz.walnut.ext.media.dom.DomContext;
import org.nutz.walnut.ext.media.dom.DomFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class dom_html extends DomFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(body)$");
    }

    @Override
    protected void process(WnSystem sys, DomContext fc, ZParams params) {
        fc.loadHtml(params.vals, false, params.is("body"));
    }

}
