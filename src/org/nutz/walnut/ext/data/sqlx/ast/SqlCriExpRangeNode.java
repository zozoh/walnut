package org.nutz.walnut.ext.data.sqlx.ast;

import java.util.Date;
import java.util.List;

import org.nutz.lang.util.DateRegion;
import org.nutz.lang.util.FloatRegion;
import org.nutz.lang.util.IntRegion;
import org.nutz.lang.util.LongRegion;
import org.nutz.lang.util.Region;
import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqls;
import org.nutz.walnut.util.Wregion;

public class SqlCriExpRangeNode extends SqlCriExpressionNode {

    public static SqlCriExpRangeNode tryParse(String name, String s) {
        // 整数范围
        if (s.matches(Wregion.intRegion())) {
            IntRegion rg = Region.Int(s);
            return new SqlCriExpRangeNode(name, rg);
        }
        // 长整数范围
        if (s.matches(Wregion.longRegion())) {
            LongRegion rg = Region.Long(s);
            return new SqlCriExpRangeNode(name, rg);
        }
        // 浮点范围
        if (s.matches(Wregion.floatRegion())) {
            FloatRegion rg = Region.Float(s);
            return new SqlCriExpRangeNode(name, rg);
        }
        // 日期范围
        if (s.matches(Wregion.dateRegion("^[Dd]ate"))) {
            String s2 = s.substring(4).trim();
            s2 = Wregion.extend_rg_macro(s2);
            DateRegion rg = Region.Date(s2);
            return new SqlCriExpRangeNode(name, rg);
        }
        // 日期范围当做毫秒数
        else if (s.matches(Wregion.dateRegion("^[Mm][Ss]"))) {
            String str = s.substring(2);
            DateRegion rg = Region.Date(str);

            LongRegion rg2 = new LongRegion();
            rg2.leftOpen(rg.isLeftOpen()).rightOpen(rg.isRightOpen());

            Date l = rg.left();
            if (null != l)
                rg2.left(l.getTime());

            Date r = rg.right();
            if (null != r)
                rg2.right(r.getTime());

            return new SqlCriExpRangeNode(name, rg);
        }
        // 不能接受这个值
        return null;
    }

    private Region<?> range;

    public SqlCriExpRangeNode(String name, Region<?> range) {
        super(name);
        this.range = range;
    }

    @Override
    public void joinParams(List<Object> params) {
        if (range.isRegion()) {
            if (null != range.left()) {
                params.add(range.left());
            }
            if (null != range.right()) {
                params.add(range.right());
            }
        }
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        // 采用语句参数
        if (useParams) {
            if (null != range.left()) {
                sb.append(this.name);
                sb.append(range.leftOpt(">", ">="));
                sb.append("?");
            }
            if (null != range.right()) {
                sb.append(this.name);
                sb.append(range.rightOpt("<", "<="));
                sb.append("?");
            }
        }
        // 采用普通语句
        else {
            if (null != range.left()) {
                String left = WnSqls.valueToSqlExp(range.left());
                sb.append(this.name);
                sb.append(range.leftOpt(">", ">="));
                sb.append(left);
            }
            if (null != range.right()) {
                String right = WnSqls.valueToSqlExp(range.right());
                sb.append(this.name);
                sb.append(range.rightOpt("<", "<="));
                sb.append(right);
            }
        }
    }

}
