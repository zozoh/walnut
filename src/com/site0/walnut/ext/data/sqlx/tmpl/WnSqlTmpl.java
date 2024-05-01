package com.site0.walnut.ext.data.sqlx.tmpl;

import java.util.List;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.ext.data.sqlx.loader.SqlEntry;
import com.site0.walnut.ext.data.sqlx.loader.SqlType;
import com.site0.walnut.util.tmpl.WnTmplParsing;
import com.site0.walnut.util.tmpl.WnTmplX;

public class WnSqlTmpl {

    public static WnSqlTmpl parse(SqlEntry sqle) {
        WnSqlElementMaker tknMaker = new WnSqlElementMaker(sqle);
        String input = sqle.getContent();
        WnSqlTmpl re = parse(input, tknMaker);
        if (sqle.hasType()) {
            re.type = sqle.getType();
        }
        return re;
    }

    public static WnSqlTmpl parse(String input) {
        WnSqlElementMaker tknMaker = new WnSqlElementMaker();
        return parse(input, tknMaker);
    }

    public static WnSqlTmpl parse(String input, WnSqlElementMaker tknMaker) {
        char[] cs = input.toCharArray();
        WnTmplParsing ing = new WnTmplParsing(tknMaker);
        ing.setExpert(null);
        WnTmplX tmpl = ing.parse(cs);
        WnSqlTmpl re = new WnSqlTmpl(tmpl);
        re.type = WnSqls.autoSqlType(input);
        return re;
    }

    private SqlType type;
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
    public String render(NutBean context, List<SqlParam> params) {
        SqlRenderContext rc = new SqlRenderContext();
        rc.context = context;
        rc.showKey = true;
        rc.params = params;
        tmpl.renderTo(rc);
        return rc.out.toString();
    }

    public String toString() {
        return tmpl.toString();
    }

    public boolean isSELECT() {
        return SqlType.SELECT == this.type;
    }

    public boolean isUPDATE() {
        return SqlType.UPDATE == this.type;
    }

    public boolean isDELETE() {
        return SqlType.DELETE == this.type;
    }

    public boolean isINSERT() {
        return SqlType.INSERT == this.type;
    }

    public SqlType getType() {
        return type;
    }

}
