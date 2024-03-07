package org.nutz.walnut.ext.data.sqlx.tmpl.vars;

import java.util.Map;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.data.sqlx.tmpl.SqlRenderContext;
import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqls;
import org.nutz.walnut.util.tmpl.WnTmplRenderContext;

public class VarsAsInsertValuesElement extends SqlVarsElement {

    public VarsAsInsertValuesElement(String content) {
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
            // 模板字段分隔符
            if (i > 0) {
                rc.sb.append(",");
            }
            // 记入模板字段和动态参数
            if (null != src && null != src.params) {
                src.params.add(en.getValue());
                src.sb.append('?');
            }
            // 采用传统的 SQL 方式
            else {
                Object val = en.getValue();
                String vs = WnSqls.valueToSqlExp(val);
                rc.sb.append(vs);
            }

            // 计数
            i++;
        }
    }

}
