package org.nutz.walnut.ext.data.sqlx.srv.vars;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.tmpl.WnTmplRenderContext;

public class VarsAsInsertValuesElement extends SqlVarsElement {

    public VarsAsInsertValuesElement(String content) {
        super(content);
    }

    @Override
    public void join(WnTmplRenderContext rc) {
        NutBean bean = this.getBean(rc.context);
        for (int i = 0; i < bean.size(); i++) {
            if (i > 0) {
                rc.sb.append(",");
            }
            rc.sb.append('?');
        }
    }

    @Override
    public void joinParams(NutBean context, List<String> params) {
        NutBean bean = this.getBean(context);
        params.addAll(bean.keySet());
    }

}
