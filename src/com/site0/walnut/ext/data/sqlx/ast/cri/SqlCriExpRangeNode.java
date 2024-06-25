package com.site0.walnut.ext.data.sqlx.ast.cri;

import java.util.Date;
import java.util.List;

import org.nutz.lang.util.DateRegion;
import org.nutz.lang.util.FloatRegion;
import org.nutz.lang.util.IntRegion;
import org.nutz.lang.util.LongRegion;
import org.nutz.lang.util.Region;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.util.Wregion;

public class SqlCriExpRangeNode extends SqlCriExpressionNode {

    public static SqlCriExpRangeNode tryParse(String name, String str) {
        String s = Wregion.extend_rg_macro(str);
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
            String s2 = s.substring(2);
            DateRegion rg = Region.Date(s2);

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

    private SqlCriExpSimpleNode left;

    private SqlCriExpSimpleNode right;

    private SqlCriExpSimpleEqNode eq;

    public SqlCriExpRangeNode(String name, Region<?> range) {
        super(name);
        if (!range.isNull()) {
            // 区间
            if (range.isRegion()) {
                Object l = range.left();
                if (null != l) {
                    if (range.isLeftOpen()) {
                        this.left = new SqlCriExpSimpleGtNode(name, l);
                    } else {
                        this.left = new SqlCriExpSimpleGteNode(name, l);
                    }
                }

                Object r = range.right();
                if (null != r) {
                    if (range.isRightOpen()) {
                        this.right = new SqlCriExpSimpleLtNode(name, r);
                    } else {
                        this.right = new SqlCriExpSimpleLteNode(name, r);
                    }
                }
            }
            // 精确匹配
            else {
                Object l = range.left();
                this.eq = new SqlCriExpSimpleEqNode(name, l);
            }
        }
    }

    @Override
    protected void _join_self_params(List<SqlParam> params) {
        // 精确匹配
        if (null != eq) {
            eq._join_self_params(params);
        }
        // 范围
        else {
            if (null != left) {
                left._join_self_params(params);
            }
            if (null != right) {
                right._join_self_params(params);
            }
        }
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        // 精确匹配
        if (null != eq) {
            this.eq._join_self(sb, useParams);
        }
        // 范围
        else {
            if (null != left && null != right) {
                sb.append("(");
                left._join_self(sb, useParams);
                sb.append(" AND ");
                right._join_self(sb, useParams);
                sb.append(")");
            }
            // 仅左值
            else if (null != left) {
                left._join_self(sb, useParams);
            }
            // 仅右值
            else if (null != right) {
                right._join_self(sb, useParams);
            }
        }

    }

}
