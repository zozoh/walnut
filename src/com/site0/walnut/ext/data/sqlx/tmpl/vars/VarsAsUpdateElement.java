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
            Object val = en.getValue();
            String vs = null == val ? null : val.toString();

            // 模板字段分隔符
            if (i > 0) {
                rc.out.append(",");
            }

            // 对于直接设置的值
            if (null != vs && vs.startsWith(":=>")) {
                String str = vs.substring(3).trim();
                str = Sqlx.escapeSqlValue(str);
                rc.out.append(key).append('=').append(str);
            }
            // 记入动态参数
            else if (null != src && null != src.params) {
                src.params.add(new SqlParam(key, val, this.scope));
                src.out.append(key).append("=?");
            }
            // 采用传统的 SQL 方式
            else {
                String vexs = Sqlx.valueToSqlExp(val);
                rc.out.append(key).append('=').append(vexs);
            }

            // 计数
            i++;
        }
    }

}
