package com.site0.walnut.ext.data.sqlx.tmpl.vars;

import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;

import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlRenderContext;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;
import com.site0.walnut.util.tmpl.WnTmplRenderContext;
import com.site0.walnut.util.tmpl.WnTmplTokenExpert;
import com.site0.walnut.util.tmpl.WnTmplX;

/**
 * 支持参数占位符
 * 
 * <pre>
 * SELECT * FROM t_pet WHERE name LIKE ${@vars=param; name=hint; format=%$[hint]%}
 * # 其中:
 * - `@vars=param` 标识了占位符的类型，它在渲染中将输出 `?` 作为预渲染模板的占位符
 * - `name=hint` 标识将从已经处理过的 bean 里获取 `hint` 键的内容作为渲染值
 * - `format=%$[hint]%` 是一个值的格式化模板，假设上下文中 hint 值为 'Be' 它最终将渲染 '%Be%
 *           这可以适用比较复杂的情况,它的优先级高于 name
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class VarsAsParamElement extends SqlVarsElement {

    private String name;

    private WnTmplX format;

    public VarsAsParamElement(String content) {
        super(content);
    }

    @Override
    protected boolean acceptSetup(String key, String val) {
        if ("name".equals(key)) {
            name = val;
            return true;
        }
        if ("format".equals(key)) {
            WnTmplTokenExpert expert = new WnTmplTokenExpert("$$", "$[", ']');
            format = WnTmplX.parse(expert, val);
            return true;
        }
        return false;
    }

    @Override
    public void join(WnTmplRenderContext rc) {
        SqlRenderContext src = null;
        if (rc instanceof SqlRenderContext) {
            src = (SqlRenderContext) rc;
        }
        NutBean bean = this.getBean(rc.context);

        // 获取值
        Object val = null;
        // 采用格式化模板
        if (null != format) {
            val = format.render(bean);
        }
        // 直接获取值
        else if (null != name) {
            val = Mapl.cell(bean, name);
        }

        // 记入模板字段和动态参数
        if (null != src && null != src.params) {
            src.params.add(new SqlParam(name, val, this.scope));
            src.out.append('?');
        }
        // 采用传统的 SQL 方式
        else {
            String vs = Sqlx.valueToSqlExp(val);
            rc.out.append(vs);
        }

    }

}
