package org.nutz.walnut.ext.data.sqlx.srv.vars;

import java.util.List;
import java.util.Map;

import org.nutz.lang.util.NutBean;

public class VarsAsWhereElement extends SqlVarsElement {

    public VarsAsWhereElement(String content) {
        super(content);
    }

    @Override
    public void join(StringBuilder sb, NutBean context, boolean showKey) {
        NutBean bean = this.getBean(context);
        int i = 0;
        for (Map.Entry<String, Object> en : bean.entrySet()) {
            sb.append(en.getKey());
            if (i > 0) {
                sb.append(", ");
            }
            i++;
        }
    }

    @Override
    public void joinParams(NutBean context, List<String> params) {
        // 插入列头，无需任何参数
    }

}
