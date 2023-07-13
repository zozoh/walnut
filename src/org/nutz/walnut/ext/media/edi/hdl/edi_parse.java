package org.nutz.walnut.ext.media.edi.hdl;

import org.nutz.walnut.ext.media.edi.EdiContext;
import org.nutz.walnut.ext.media.edi.EdiFilter;
import org.nutz.walnut.ext.media.edi.bean.EdiInterchange;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class edi_parse extends EdiFilter {

    @Override
    protected void process(WnSystem sys, EdiContext fc, ZParams params) {
        fc.assertMessage();
        fc.ic = EdiInterchange.parse(fc.message);
    }

}
