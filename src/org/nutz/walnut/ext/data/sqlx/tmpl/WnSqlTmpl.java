package org.nutz.walnut.ext.data.sqlx.tmpl;

import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.tmpl.WnTmplParsing;
import org.nutz.walnut.util.tmpl.WnTmplX;

public class WnSqlTmpl {

    public static WnSqlTmpl parse(String input) {
        WnSqlElementMaker tknMaker = new WnSqlElementMaker();
        return parse(input, tknMaker);
    }

    public static WnSqlTmpl parse(String input, WnSqlElementMaker tknMaker) {
        char[] cs = input.toCharArray();
        WnTmplParsing ing = new WnTmplParsing(tknMaker);
        ing.setExpert(null);
        WnTmplX tmpl = ing.parse(cs);

        return new WnSqlTmpl(tmpl);
    }

    private WnTmplX tmpl;

    private WnSqlTmpl(WnTmplX tmpl) {
        this.tmpl = tmpl;
    }

    /**
     * 对 SQL 模板进行渲染，同时如果声明了 <code>${@vars}</code> 占位符，也会渲染出 PreparedStatement
     * 的参数表
     * 
     * @param context
     *            变量上下文
     * @param params
     *            参数表，如果传 null，则无视,将模板转换为传统的 SQL 语句
     * @return 渲染后的 SQL 模板
     */
    public String render(NutBean context, List<String> params) {
        SqlRenderContext rc = new SqlRenderContext();
        rc.context = context;
        rc.showKey = true;
        rc.params = params;
        tmpl.renderTo(rc);
        return rc.sb.toString();
    }
    

    public String toString() {
        return tmpl.toString();
    }

}
