package org.nutz.walnut.ext.data.sqlx.srv.vars;

import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.data.sqlx.srv.SqlRenderContext;
import org.nutz.walnut.util.tmpl.WnTmplRenderContext;

public class VarsAsUpdateElement extends SqlVarsElement {

    public VarsAsUpdateElement(String content) {
        super(content);
    }

    @Override
    public void join(WnTmplRenderContext rc) {
        SqlRenderContext src = null;
        if (rc instanceof SqlRenderContext) {
            src = (SqlRenderContext) rc;
        }
        NutBean bean = this.getBean(rc.context);
        int i = 0;
        for (Map.Entry<String, Object> en : bean.entrySet()) {
            if (null != src) {
                src.params.add(en.getKey());
            }
            if (i > 0) {
                rc.sb.append(",");
            }
            rc.sb.append(en.getKey()).append("=?");
            i++;
        }
    }

    @Override
    public void joinParams(NutBean context, List<String> params) {
        NutBean bean = this.getBean(context);
        params.addAll(bean.keySet());
    }

}
