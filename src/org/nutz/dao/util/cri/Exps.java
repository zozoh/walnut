package org.nutz.dao.util.cri;

import java.util.Collection;

import org.nutz.castor.Castors;
import org.nutz.dao.util.lambda.LambdaQuery;
import org.nutz.dao.util.lambda.PFun;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;

/**
 * 表达式的帮助函数
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class Exps {

    public static SqlExpressionGroup begin() {
        return new SqlExpressionGroup();
    }

    public static Like like(String name, String value) {
        return Like.create(name, value, true);
    }

    public static <T> Like like(PFun<T, ?> name, String value) {
        return Like.create(name, value, true);
    }

    public static Like like(String name, String value, boolean ignoreCase) {
        return Like.create(name, value, ignoreCase);
    }

    public static <T> Like like(PFun<T, ?> name, String value, boolean ignoreCase) {
        return Like.create(name, value, ignoreCase);
    }

    public static IsNull isNull(String name) {
        return new IsNull(name);
    }

    public static <T> IsNull isNull(PFun<T, ?> name) {
        return new IsNull(name);
    }

    public static SimpleExpression eq(String name, Object val) {
        return new SimpleExpression(name, "=", val);
    }

    public static <T> SimpleExpression eq(PFun<T, ?> name, Object val) {
        return new SimpleExpression(name, "=", val);
    }

    public static SimpleExpression gt(String name, long val) {
        return new SimpleExpression(name, ">", val);
    }

    public static <T> SimpleExpression gt(PFun<T, ?> name, long val) {
        return new SimpleExpression(name, ">", val);
    }

    public static SimpleExpression lt(String name, long val) {
        return new SimpleExpression(name, "<", val);
    }

    public static <T> SimpleExpression lt(PFun<T, ?> name, long val) {
        return new SimpleExpression(name, "<", val);
    }

    public static SimpleExpression gte(String name, long val) {
        return new SimpleExpression(name, ">=", val);
    }

    public static <T> SimpleExpression gte(PFun<T, ?> name, long val) {
        return new SimpleExpression(name, ">=", val);
    }

    public static SimpleExpression lte(String name, long val) {
        return new SimpleExpression(name, "<=", val);
    }

    public static <T> SimpleExpression lte(PFun<T, ?> name, long val) {
        return new SimpleExpression(name, "<=", val);
    }

    public static IntRange inInt(String name, int... ids) {
        return new IntRange(name, ids);
    }

    public static <T> IntRange inInt(PFun<T, ?> name, int... ids) {
        return new IntRange(LambdaQuery.resolve(name), ids);
    }

    public static IntRange inInt(String name, Integer[] ids) {
    	return new IntRange(name, ids);
    }

    public static <T> IntRange inInt(PFun<T, ?> name, Integer[] ids) {
    	return new IntRange(LambdaQuery.resolve(name), ids);
    }

    public static LongRange inLong(String name, long... ids) {
        return new LongRange(name, ids);
    }

    public static <T> LongRange inLong(PFun<T, ?> name, long... ids) {
        return new LongRange(LambdaQuery.resolve(name), ids);
    }

    public static LongRange inLong(String name, Long[] ids) {
    	return new LongRange(name, ids);
    }

    public static <T> LongRange inLong(PFun<T, ?> name, Long[] ids) {
    	return new LongRange(LambdaQuery.resolve(name), ids);
    }

    public static NameRange inStr(String name, String... names) {
        return new NameRange(name, names);
    }

    public static <T> NameRange inStr(PFun<T, ?> name, String... names) {
        return new NameRange(name, names);
    }

    public static SqlRange inSql(String name, String subSql, Object... args) {
        return new SqlRange(name, subSql, args);
    }

    public static <T> SqlRange inSql(PFun<T, ?> name, String subSql, Object... args) {
        return new SqlRange(name, subSql, args);
    }

    public static SqlValueRange inSql2(String name, String subSql, Object... values) {
        return new SqlValueRange(name, subSql, values);
    }

    public static <T> SqlValueRange inSql2(PFun<T, ?> name, String subSql, Object... values) {
        return new SqlValueRange(name, subSql, values);
    }

    public static SqlValueRange inSql2(String name, String subSql, Collection<?> collection) {
        return new SqlValueRange(name, subSql, collection.toArray());
    }

    public static <T> SqlValueRange inSql2(PFun<T, ?> name, String subSql, Collection<?> collection) {
        return new SqlValueRange(name, subSql, collection.toArray());
    }

    public static <T> SqlExpression create(PFun<T, ?> name, String op, Object value) {
        return create(LambdaQuery.resolve(name), op, value);
    }

    public static SqlExpression create(String name, String op, Object value) {
        op = Strings.trim(op.toUpperCase());

        // NULL
        if (null == value) {
            SqlExpression re;
            // IS NULL
            if ("=".equals(op) || "IS".equals(op) || "NOT IS".equals(op) || "IS NOT".equals(op)) {
                re = isNull(name);
            }
            // !!!
            else {
                throw Wlang.makeThrow("null can only use 'IS' or 'NOT IS'");
            }
            return re.setNot(op.startsWith("NOT") || op.endsWith("NOT"));
        }
        // IN
        else if ("IN".equals(op) || "NOT IN".equals(op)) {
            Class<?> type = value.getClass();
            SqlExpression re;
            int len = Wlang.eleSize(value);
            if (len < 1) { // 如果空数组/空集合,则返回 @since 1.r.57
                re = new Static("1 != 1");
            }
            // 数组
            else if (type.isArray()) {
                re = _evalRange((Mirror<?>) Mirror.me(type.getComponentType()), name, value);
            }
            // 集合
            else if (Collection.class.isAssignableFrom(type)) {
                Object first = Wlang.firstInAny(value);
                if (null == first)
                    return null;
                re = _evalRange((Mirror<?>) Mirror.me(first), name, value);
            }
            // Sql Range
            else {
                re = inSql(name, value.toString());
            }
            return re.setNot(op.startsWith("NOT"));
        }
        // LIKE || IS
        else if ("LIKE".equals(op) || "NOT LIKE".equals(op)) {
            String v = value.toString();
            Like re;
            if (v.length() == 1) {
                re = like(name, v);
            } else {
                re = like(name, v.substring(1, v.length() - 1));
                re.left(v.substring(0, 1));
                re.right(v.substring(v.length() - 1, v.length()));
            }
            return re.ignoreCase(false).setNot(op.startsWith("NOT"));
        }
        // =
        else if ("=".equals(op)) {
            return eq(name, value);
        }
        // !=
        else if ("!=".equals(op) || "<>".equals(op)) {// TODO 检查一下,原本是&&, 明显永远成立
            return eq(name, value).setNot(true);
        } else if ("BETWEEN".equals(op)) {
        	Object[] values = (Object[])value;
        	return new BetweenExpression(name, values[0], values[1]);
        }
        // Others
        return new SimpleExpression(name, op, value);
    }

    private static SqlExpression _evalRange(Mirror<?> mirror, String name, Object value) {
        if (mirror.isInt())
            return inInt(name, Castors.me().castTo(value, int[].class));

        else if (mirror.isLong())
            return inLong(name, Castors.me().castTo(value, long[].class));

        return inStr(name, Castors.me().castTo(value, String[].class));
    }

}
