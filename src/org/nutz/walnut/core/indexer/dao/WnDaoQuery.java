package org.nutz.walnut.core.indexer.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.dao.Cnd;
import org.nutz.dao.Condition;
import org.nutz.dao.entity.Entity;
import org.nutz.dao.entity.MappingField;
import org.nutz.dao.pager.Pager;
import org.nutz.dao.util.cri.Exps;
import org.nutz.dao.util.cri.SimpleCriteria;
import org.nutz.dao.util.cri.SimpleExpression;
import org.nutz.dao.util.cri.SqlExpression;
import org.nutz.dao.util.cri.SqlExpressionGroup;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.DateRegion;
import org.nutz.lang.util.FloatRegion;
import org.nutz.lang.util.IntRegion;
import org.nutz.lang.util.LongRegion;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.util.WnRg;

public class WnDaoQuery {

    private WnQuery q;

    private Entity<WnIoObj> entity;

    public WnDaoQuery(WnQuery q, WnObjEntity entity) {
        this.q = q;
        this.entity = entity;

    }

    public Condition getCondition() {
        // 空
        if (null == q || q.isEmptyMatch()) {
            return null;
        }

        SimpleCriteria cri = Cnd.cri();
        SqlExpressionGroup top = cri.where();

        // 循环获取
        List<NutMap> list = q.getList();

        // 多个条件，那么之间是 or 的关系
        // 因为是 SQL 的缘故，第一个 or 不输出
        // 也就是说，只有一个顶级条件的话， and 和 or 是无所谓的
        for (NutMap map : list) {
            // 填充表达式
            SqlExpressionGroup grp = topMapToExp(map);
            // 计入顶级
            if (null != grp && !grp.isEmpty())
                top.or(grp);
        }

        // 搞定
        return cri;
    }

    // Map 表示的一组条件自然都是 AND 的关系
    SqlExpressionGroup topMapToExp(NutMap map) {
        SqlExpressionGroup grp = new SqlExpressionGroup();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            // 如果顶级条件是 "lbls"，那么自动拆解值为数组
            if ("lbls".equals(key) && val instanceof CharSequence) {
                String[] ss = Strings.splitIgnoreBlank(val.toString(), "[ ,，\t;；]");
                if (0 == ss.length) {
                    val = null;
                } else {
                    val = Lang.arrayFirst("all", ss);
                }
            }
            // 设置到查询条件中
            SqlExpression exp = anyToExp(key, val);
            if (null != exp) {
                grp.and(exp);
            }
        }
        if (grp.isEmpty())
            return null;
        return grp;
    }

    @SuppressWarnings("unchecked")
    SqlExpression anyToExp(String key, Object val) {
        // 首先翻译一下数据库字段
        String colName = evalColName(key);

        // 这个字段没有被声明，那么从查询条件里移除
        if (null == colName)
            return null;

        // 按条件判断值，决定采用哪种表达式
        if (null == val) {
            return Exps.isNull(colName);
        }
        // 如果是空串
        if ("".equals(val)) {
            return Exps.isNull(colName).setNot(true);
        }
        // 数字类型
        if (val instanceof Number) {
            return Exps.eq(colName, val);
        }
        // 范围
        if (val instanceof Region) {
            return regionToExp(colName, (Region<?>) val);
        }
        // Regex
        if (val instanceof Pattern) {
            return regexToExp(colName, val.toString());
        }
        // Collection
        if (val instanceof Collection) {
            Collection<Object> col = (Collection<Object>) val;
            Object[] vv = col.toArray(new Object[col.size()]);
            return arrayToExp(colName, vv);
        }
        // Array
        if (val.getClass().isArray()) {
            return arrayToExp(colName, (Object[]) val);
        }
        // Map
        // 支持一些特殊的语法 "[%$](eq|ne|gt|gte|lt|lte|in|nin)" 等
        if (val instanceof Map) {
            return mapToExp(colName, val);
        }
        //
        // Simple value
        //
        Mirror<?> mi = Mirror.me(val);
        // 字符串
        if (mi.isStringLike()) {
            String s = val.toString();
            // 非空
            if ("".equals(s)) {
                return Exps.isNull(colName).setNot(true);
            }

            // 如果是范围，那么默认的，那么展开里面的内容
            s = WnRg.extend_rg_macro(s);

            // 正则表达式
            if (s.startsWith("^") || s.startsWith("!^")) {
                return this.regexToExp(colName, s);
            }
            // 表示不等于
            if (s.startsWith("!")) {
                // 直接是 "!" 表示不等于 null
                if ("!".equals(s)) {
                    return Exps.isNull(colName).setNot(true);
                }
                // 否则取值
                return Exps.eq(colName, s.substring(1)).setNot(true);
            }
            // 通配符
            if (s.contains("*") || s.contains("?")) {
                String s2 = s.replace('*', '%').replace('?', '_');
                return Exps.create(colName, "LIKE", s2);
            }
            // 整数范围
            if (s.matches(WnRg.intRegion())) {
                IntRegion rg = Region.Int(s);
                return this.regionToExp(colName, rg);
            }
            // 长整数范围
            if (s.matches(WnRg.longRegion())) {
                LongRegion rg = Region.Long(s);
                return this.regionToExp(colName, rg);
            }
            // 浮点范围
            if (s.matches(WnRg.floatRegion())) {
                FloatRegion rg = Region.Float(s);
                return this.regionToExp(colName, rg);
            }
            // 日期范围
            if (s.matches(WnRg.dateRegion("^"))) {
                DateRegion rg = Region.Date(s);
                return this.regionToExp(colName, rg);
            }
            // 日期范围当做毫秒数
            else if (s.matches(WnRg.dateRegion("^[Mm][Ss]"))) {
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

                return this.regionToExp(colName, rg2);
            }
            // 普通字符串
            return Exps.eq(colName, s);
        }
        // 其他直接搞吧
        return Exps.eq(colName, val);
    }

    @SuppressWarnings("unchecked")
    private SqlExpression mapToExp(String colName, Object val) {
        SqlExpressionGroup grp = new SqlExpressionGroup();
        NutMap map = NutMap.WRAP((Map<String, Object>) val);
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String k = en.getKey();
            Matcher m = _P2.matcher(k);
            if (m.find()) {
                String md = m.group(1);
                Object v = en.getValue();
                // $eq
                if ("eq".equals(md)) {
                    grp.andEquals(colName, v);
                }
                // $ne
                else if ("ne".equals(md)) {
                    grp.andNotEquals(colName, v);
                }
                // $lt
                else if ("lt".equals(md)) {
                    long vl = Castors.me().castTo(v, Long.class);
                    grp.andLT(colName, vl);
                }
                // $lte
                else if ("lte".equals(md)) {
                    long vl = Castors.me().castTo(v, Long.class);
                    grp.andLTE(colName, vl);
                }
                // $gt
                else if ("gt".equals(md)) {
                    long vl = Castors.me().castTo(v, Long.class);
                    grp.andGT(colName, vl);
                }
                // $gte
                else if ("gte".equals(md)) {
                    long vl = Castors.me().castTo(v, Long.class);
                    grp.andGTE(colName, vl);
                }
                // $in
                else if ("in".equals(md)) {
                    Object[] vv = Castors.me().castTo(v, Object[].class);
                    SqlExpression e2 = enumArrayToExp(colName, vv, "in");
                    grp.and(e2);
                }
                // $nin
                else if ("nin".equals(md)) {
                    Object[] vv = Castors.me().castTo(v, Object[].class);
                    SqlExpression e2 = enumArrayToExp(colName, vv, "nin");
                    grp.and(e2);
                }
            }
        }
        if (grp.isEmpty())
            return null;
        return grp;
    }

    private String evalColName(String stdName) {
        // [$%](and|or) 这种键有特殊含义，不用检查
        if (_P.matcher(stdName).find()) {
            return stdName;
        }
        MappingField mf = entity.getField(stdName);
        if (null == mf) {
            // throw Er.create("e.io.dao.FieldNotDefined", stdName);
            return null;
        }
        return mf.getColumnName();
    }

    private static final Pattern _P2 = Pattern.compile("^[$%](eq|ne|gt|gte|lt|lte|in|nin)$");

    private static final Pattern _P = Pattern.compile("^[$%](n?in|all|and|or)$");

    @SuppressWarnings("unchecked")
    private SqlExpression arrayToExp(String colName, Object[] vv) {
        if (vv.length <= 0) {
            return null;
        }

        Mirror<?> mi = Mirror.me(vv[0]);

        // 是联合的查询
        if (mi.isMap()) {
            Matcher m = _P.matcher(colName);
            // 那么 key 必须是 $and 或者 $or
            if (m.find()) {
                SqlExpressionGroup grp = new SqlExpressionGroup();
                boolean isAnd = "and".equals(m.group(1));
                // 遍历所有的查询条件
                for (Object v2 : vv) {
                    Map<String, Object> map = (Map<String, Object>) v2;
                    for (Map.Entry<String, Object> en : map.entrySet()) {
                        String key2 = en.getKey();
                        Object val2 = en.getValue();
                        SqlExpression e2 = this.anyToExp(key2, val2);
                        if (null != e2) {
                            if (isAnd) {
                                grp.and(e2);
                            } else {
                                grp.or(e2);
                            }
                        }
                    }

                }
                if (grp.isEmpty())
                    return null;
                return grp;
            }
            // 靠，错误的 Key
            else {
                throw Er.create("e.io.query.unsuport", q.toString());
            }
        }
        // 那就作为普通元素的枚举
        return enumToExp(colName, vv);
    }

    private SqlExpression enumToExp(String colName, Object[] vv) {
        // 空数组，无视
        if (null == vv || vv.length == 0) {
            return null;
        }

        // 看看是否是 "$in": [..] 这样的结构
        Matcher m = _P.matcher(colName);
        if (m.find()) {
            String matchMode = m.group(1);
            return enumArrayToExp(colName, vv, matchMode);
        }

        // 单个值
        if (vv.length == 1) {
            return Exps.eq(colName, vv[0]);
        }
        // 多个值，看看是 “与” 还是 “或”
        Object v0 = vv[0];
        // 指明 in
        if (v0.equals("%in")) {
            Object[] vv2 = Arrays.copyOfRange(vv, 1, vv.length);
            return enumArrayToExp(colName, vv2, "in");
        }
        // 指明 all
        if (v0.equals("%all")) {
            Object[] vv2 = Arrays.copyOfRange(vv, 1, vv.length);
            return enumArrayToExp(colName, vv2, "all");
        }
        // 指明 nin
        if (v0.equals("%nin")) {
            Object[] vv2 = Arrays.copyOfRange(vv, 1, vv.length);
            return enumArrayToExp(colName, vv2, "nin");
        }
        // 指明了 or 那么全部变成条件
        if (v0.equals("%or")) {
            SqlExpressionGroup grp = new SqlExpressionGroup();
            for (int i = 1; i < vv.length; i++) {
                Object v = vv[i];
                SqlExpression exp = this.anyToExp(colName, v);
                if (null != exp)
                    grp.or(exp);
            }
            if (grp.isEmpty())
                return null;
            return grp;
        }
        // 默认用 in
        return enumArrayToExp(colName, vv, "in");
    }

    private SqlExpression enumArrayToExp(String colName, Object[] vv, String matchMode) {
        boolean notIn = false;
        if ("nin".equals(matchMode)) {
            notIn = true;
        }
        Mirror<?> mi = Mirror.me(vv[0]);
        // 整数
        if (mi.isInt()) {
            int[] iis = Castors.me().castTo(vv, int[].class);
            return Exps.inInt(colName, iis).setNot(notIn);
        }
        // 长整
        if (mi.isLong()) {
            long[] lls = Castors.me().castTo(vv, long[].class);
            return Exps.inLong(colName, lls).setNot(notIn);
        }
        // 字符串
        else if (mi.isStringLike()) {
            String[] sss = Castors.me().castTo(vv, String[].class);
            return Exps.inStr(colName, sss).setNot(notIn);
        }
        // 其他不支持
        else {
            throw Lang.noImplement();
        }
    }

    private SqlExpression regexToExp(String colName, String regex) {
        boolean not = false;
        if (regex.startsWith("!")) {
            regex = regex.substring(1).trim();
            not = true;
        }
        // TODO 这个似乎应该在多种数据库中适配，应该增强 JdbcExpert 接口
        // 现在，暂时只支持 MySQL 吧
        SimpleExpression se = new SimpleExpression(colName, "REGEXP", regex);
        se.setNot(not);
        return se;
    }

    private SqlExpression regionToExp(String colName, Region<?> rg) {
        // 如果是一个范围
        if (rg.isRegion()) {
            SqlExpressionGroup grp = new SqlExpressionGroup();
            if (rg.left() != null) {
                String op = rg.leftOpt(">", ">=");
                grp.and(new SimpleExpression(colName, op, rg.left()));
            }
            if (rg.right() != null) {
                String op = rg.rightOpt("<", "<=");
                grp.and(new SimpleExpression(colName, op, rg.right()));
            }
            return grp;
        }
        // 如果是一个精确的值
        else if (!rg.isNull()) {
            // 如果两边都是开区间表示不等于
            if (rg.isLeftOpen() && rg.isRightOpen()) {
                return Exps.eq(colName, rg.left()).setNot(true);
            }
            // 否则表示等于
            return Exps.eq(colName, rg.left());
        }
        // 不可能啊
        throw Lang.impossible();
    }

    public Pager getPager() {
        Pager pg = new Pager();
        int limit = q.limit();
        int skip = q.skip();
        if (limit > 0) {
            int pn = (skip / limit) + 1;
            pg.setPageNumber(pn);
            pg.setPageSize(limit);
        } else {
            pg.setPageNumber(0);
            pg.setPageSize(0);
        }
        return pg;
    }

}
