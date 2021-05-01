package org.nutz.walnut.ext.media.ooml.hdl;

import org.nutz.walnut.ext.media.ooml.OomlContext;
import org.nutz.walnut.ext.media.ooml.OomlFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.ooml.xlsx.XlsxWorkbook;
import org.nutz.walnut.util.ZParams;

public class ooml_xlsx extends OomlFilter {

    @Override
    protected void process(WnSystem sys, OomlContext fc, ZParams params) {
        fc.workbook = new XlsxWorkbook(fc.ooml);
    }

}
