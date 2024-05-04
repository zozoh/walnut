package com.site0.walnut.ext.data.sqlx.tmpl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.sqlx.loader.SqlEntry;
import com.site0.walnut.ext.data.sqlx.tmpl.vars.SqlVarsElement;
import com.site0.walnut.ext.data.sqlx.tmpl.vars.VarsAsInsertColumnsElement;
import com.site0.walnut.ext.data.sqlx.tmpl.vars.VarsAsInsertValuesElement;
import com.site0.walnut.ext.data.sqlx.tmpl.vars.VarsAsSorterElement;
import com.site0.walnut.ext.data.sqlx.tmpl.vars.VarsAsUpdateElement;
import com.site0.walnut.ext.data.sqlx.tmpl.vars.VarsAsWhereElement;
import com.site0.walnut.util.tmpl.WnTmplElementMaker;
import com.site0.walnut.util.tmpl.ele.TmplEle;

public class WnSqlElementMaker implements WnTmplElementMaker {

    private SqlEntry sqle;

    public WnSqlElementMaker() {}

    public WnSqlElementMaker(SqlEntry sqle) {
        this.sqle = sqle;
    }

    private SqlVarsElement wrap(SqlVarsElement ele) {
        if (null != sqle) {
            if (!ele.hasPick()) {
                ele.setPick(sqle.getDefaultPick());
            }
            if (!ele.hasOmit()) {
                ele.setOmit(sqle.getDefaultOmit());
            }
            if (!ele.hasIgnoreNil()) {
                ele.setIgnoreNil(sqle.getDefaultIgnoreNil());
            }
        }
        return ele;
    }

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
                return wrap(new VarsAsWhereElement(setup));
            }
            // ${@vars=sorter}
            if ("sort".equals(type)) {
                return wrap(new VarsAsSorterElement(setup));
            }
            // ${@vars=update}
            if ("update".equals(type)) {
                return wrap(new VarsAsUpdateElement(setup));
            }
            // ${@vars=insert.columns}
            if ("insert.columns".equals(type)) {
                return wrap(new VarsAsInsertColumnsElement(setup));
            }
            // ${@vars=insert.values}
            if ("insert.values".equals(type)) {
                return wrap(new VarsAsInsertValuesElement(setup));
            }
            throw Er.create("e.sqlx.var.invalid", str);
        }
        return null;
    }

}
