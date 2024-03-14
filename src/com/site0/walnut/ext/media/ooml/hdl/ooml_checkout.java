package com.site0.walnut.ext.media.ooml.hdl;

import com.site0.walnut.ext.media.ooml.OomlContext;
import com.site0.walnut.ext.media.ooml.OomlFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class ooml_checkout extends OomlFilter {

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        String enPath = params.val_check(0);
        fc.currentEntry = fc.ooml.getEntry(enPath);
    }

}
