package com.site0.walnut.ext.data.sqlx.tmpl.vars;

import java.util.Map;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlRenderContext;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.util.tmpl.WnTmplRenderContext;

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
            String key = en.getKey();
            if (key.startsWith("__") || key.startsWith("$")) {
                continue;
            }
            
            // 模板字段分隔符
            if (i > 0) {
                rc.out.append(",");
            }

            // 记入动态参数
            if (null != src && null != src.params) {
                src.params.add(new SqlParam(en, this.scope));
                src.out.append(key).append("=?");
            }
            // 采用传统的 SQL 方式
            else {
                Object val = en.getValue();
                String vs = Sqlx.valueToSqlExp(val);
                rc.out.append(key).append('=').append(vs);
            }

            // 计数
            i++;
        }
    }

}
