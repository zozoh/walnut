package org.nutz.walnut.ext.data.sqlx.ast;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.dao.util.cri.Exps;
import org.nutz.lang.util.Region;
import org.nutz.walnut.util.Wlang;

public abstract class SqlCriteria {

    public static SqlCriExpressionNode anyToExp(String key, Object val) {
        boolean not = false;
        if (key.startsWith("!")) {
            not = true;
            key = key.substring(1).trim();
        }
        // 准备返回值
        SqlCriExpressionNode ex;

        // 按条件判断值，决定采用哪种表达式
        if (null == val) {
            ex = new SqlCriExpNullNode(key);
        }
        // 如果是空串
        else if ("".equals(val)) {
            not = !not;
            ex = new SqlCriExpNullNode(key);
        }
        // 数字类型
        else if (val instanceof Number) {
            ex = new SqlCriExpSimpleNode(key, val);
        }
        // 范围
        else if (val instanceof Region) {
            ex = new SqlCriExpRangeNode(key, (Region<?>) val);
        }
        // Regex
        else if (val instanceof Pattern) {
            ex = new SqlCriExpRegexpNode(key, val.toString());
        }
        // Collection
        if (val instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> col = (Collection<Object>) val;
            Object[] vv = col.toArray(new Object[col.size()]);
            ex = new SqlCriExpEnumNode(key, vv);
        }
        // Array
        if (val.getClass().isArray()) {
            Object[] vv = (Object[]) val;
            ex = new SqlCriExpEnumNode(key, vv);
        }
        // Map
        // 支持一些特殊的语法 "[%$](eq|ne|gt|gte|lt|lte|in|nin)" 等
        if (val instanceof Map) {
            //return mapToExp(colName, val, not);
        }
        
        throw Wlang.impossible();
    }

}
