package org.nutz.walnut.ext.data.sqlx.srv.vars;

import java.util.List;

import org.nutz.lang.util.NutBean;

public class VarsAsInsertValuesElement extends SqlVarsElement {

    public VarsAsInsertValuesElement(String content) {
        super(content);
    }

    @Override
    public void join(StringBuilder sb, NutBean context, boolean showKey) {
        NutBean bean = this.getBean(context);
        for (int i = 0; i < bean.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append('?');
        }
    }

    @Override
    public void joinParams(NutBean context, List<String> params) {
        NutBean bean = this.getBean(context);
        params.addAll(bean.keySet());
    }

}
