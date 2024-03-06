package org.nutz.walnut.ext.data.sqlx.srv.vars;

import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.tmpl.WnTmplRenderContext;

public class VarsAsInsertColumnsElement extends SqlVarsElement {

    public VarsAsInsertColumnsElement(String content) {
        super(content);
    }

    @Override
    public void join(WnTmplRenderContext rc) {
        NutBean bean = this.getBean(rc.context);
        int i = 0;
        for (Map.Entry<String, Object> en : bean.entrySet()) {
            if (i > 0) {
                rc.sb.append(",");
            }
            rc.sb.append(en.getKey());
            i++;
        }
    }

    @Override
    public void joinParams(NutBean context, List<String> params) {}

}
