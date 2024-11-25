package com.site0.walnut.ext.media.edi.hdl;

import com.site0.walnut.ext.media.edi.EdiContext;
import com.site0.walnut.ext.media.edi.EdiFilter;
import com.site0.walnut.ext.media.edi.bean.EdiInterchange;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class edi_parse extends EdiFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(tidy)$");
    }

    @Override
    protected void process(WnSystem sys, EdiContext fc, ZParams params) {
        fc.assertMessage();

        if (params.is("tidy")) {
            fc.tidyMessage();
        }

        fc.ic = EdiInterchange.parse(fc.message);
    }

}
