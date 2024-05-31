package com.site0.walnut.ext.data.sqlx.tmpl.vars;

import java.util.Map;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.util.tmpl.WnTmplRenderContext;

public class VarsAsInsertColumnsElement extends SqlVarsElement {

    public VarsAsInsertColumnsElement(String content) {
        super(content);
    }

    @Override
    public void join(WnTmplRenderContext rc) {
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
            // 记入模板字段
            rc.out.append(key);
            // 计数
            i++;
        }
    }

}
