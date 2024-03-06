package org.nutz.walnut.ext.data.sqlx.tmpl;

import java.util.Calendar;
import java.util.Collection;

import org.nutz.json.Json;
import org.nutz.lang.Mirror;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.Wtime;

/**
 * 构建对 SQL 处理的帮助方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class WnSqls {

    public static String escapeSqlValue(String s) {
        return s.replaceAll("'", "''");
    }

    public static String valueToSqlExp(Object val) {
        if (null == val) {
            return "NULL";
        }
        Mirror<?> mi = Mirror.me(val);

        // 布尔
        if (mi.isBoolean()) {
            return ((Boolean) val).booleanValue() ? "1" : "0";
        }

        // 数字
        if (mi.isNumber()) {
            return val.toString();
        }

        // 日期时间
        if (mi.isDateTimeLike()) {
            Calendar c = Wtime.parseAnyCalendar(val);
            return Wtime.format(c, "''yyyy-MM-dd HH:mm:ss''");
        }

        // 下面就用字符串来处理
        String str;

        // 数组
        if (mi.isArray()) {
            str = Ws.join((Object[]) val, ",");
        }
        // 集合
        else if (mi.isCollection()) {
            str = Ws.join((Collection<?>) val, ",");
        }
        // 复杂对象
        else if (mi.isMap()) {
            str = Json.toJson(val);
        }
        // 默认当作字符串
        else {
            str = val.toString();
        }

        // 最后逃逸一下
        return "'" + escapeSqlValue(str) + "'";
    }

}
