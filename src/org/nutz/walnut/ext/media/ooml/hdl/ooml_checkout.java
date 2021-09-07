package org.nutz.walnut.ext.media.ooml.hdl;

import org.nutz.walnut.ext.media.ooml.OomlContext;
import org.nutz.walnut.ext.media.ooml.OomlFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class ooml_checkout extends OomlFilter {

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        String enPath = params.val_check(0);
        fc.currentEntry = fc.ooml.getEntry(enPath);
    }

}
