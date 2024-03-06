package org.nutz.walnut.ext.data.sqlx.tmpl;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.data.sqlx.tmpl.vars.VarsAsInsertColumnsElement;
import org.nutz.walnut.ext.data.sqlx.tmpl.vars.VarsAsInsertValuesElement;
import org.nutz.walnut.ext.data.sqlx.tmpl.vars.VarsAsUpdateElement;
import org.nutz.walnut.ext.data.sqlx.tmpl.vars.VarsAsWhereElement;
import org.nutz.walnut.util.tmpl.WnTmplElementMaker;
import org.nutz.walnut.util.tmpl.ele.TmplEle;

public class WnSqlElementMaker implements WnTmplElementMaker {

    @Override
    public TmplEle make(String str) {
        if (str.startsWith("@vars=")) {
            String type;
            String setup;
            int pos = str.indexOf(';');
            if (pos > 0) {
                type = str.substring(6, pos);
                setup = str.substring(pos + 1).trim();
            } else {
                type = str.substring(6);
                setup = null;
            }
            // ${@vars=where}
            if ("where".equals(type)) {
                return new VarsAsWhereElement(setup);
            }
            // ${@vars=update}
            if ("update".equals(type)) {
                return new VarsAsUpdateElement(setup);
            }
            // ${@vars=insert.columns}
            if ("insert.columns".equals(type)) {
                return new VarsAsInsertColumnsElement(setup);
            }
            // ${@vars=insert.values}
            if ("insert.values".equals(type)) {
                return new VarsAsInsertValuesElement(setup);
            }
            throw Er.create("e.sqlx.var.invalid", str);
        }
        return null;
    }

}
