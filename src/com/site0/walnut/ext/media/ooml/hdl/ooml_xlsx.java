package com.site0.walnut.ext.media.ooml.hdl;

import com.site0.walnut.ext.media.ooml.OomlContext;
import com.site0.walnut.ext.media.ooml.OomlFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.ooml.xlsx.XlsxWorkbook;
import com.site0.walnut.util.ZParams;

public class ooml_xlsx extends OomlFilter {

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        fc.workbook = new XlsxWorkbook(fc.ooml);
    }

}
