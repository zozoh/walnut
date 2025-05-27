package com.site0.walnut.ext.data.sqlx.tmpl.vars;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;

import com.site0.walnut.ext.data.sqlx.ast.SqlCriteria;
import com.site0.walnut.ext.data.sqlx.ast.SqlCriteriaNode;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlRenderContext;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmplRenderContext;

public class VarsAsWhereElement extends SqlVarsElement {

    private Object dftInput;

    public VarsAsWhereElement(String content) {
        super(content);
        if (null != this.defaultValue) {
            this.dftInput = Json.fromJson(this.defaultValue);
        }
    }

    @Override
    public void join(WnTmplRenderContext rc) {
        SqlRenderContext src = null;
        if (rc instanceof SqlRenderContext) {
            src = (SqlRenderContext) rc;
        }
        Object input = this.getObject(rc.context);
        if (null == input) {
            input = dftInput;
        }

        SqlCriteriaNode cri = SqlCriteria.toCriNode(input);
        // 防空
        if (cri.isEmpty()) {
            return;
        }
        // 设置前缀，如果有的话
        if (!Ws.isBlank(this.fieldPrefix)) {
            cri.setFieldPrefix(fieldPrefix);
        }

        // 计入整体动态前缀
        if (!Ws.isBlank(this.prefix)) {
            src.out.append(' ').append(this.prefix).append(' ');
        }

        // 记入动态参数
        if (null != src && null != src.params) {
            cri.joinTmpl(src.out, true);

            // 将参数搞进去
            List<SqlParam> ps = new LinkedList<>();
            cri.joinParams(ps);
            SqlParam.setScopeTo(ps, this.scope);
            src.params.addAll(ps);
        }
        // 采用传统的 SQL 方式
        else {
            cri.joinTmpl(rc.out, false);
        }
    }

}
