package com.site0.walnut.util.validate.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nutz.web.WebException;

import java.lang.reflect.Array;
import java.math.BigDecimal;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.validate.WnMatch;

/**
 * 将一个对象转换为任意的匹配
 * 
 * <ul>
 * <li><code>Array|List</code> 并联(OR)
 * <li><code>Map</code> 与(AND)
 * <li><code>其他</code> 一个具体的匹配条件
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AutoMatch implements WnMatch {

    public static WnMatch parse(Object input) {
        return parse(input, false);
    }

    public static WnMatch parse(Object input, boolean dft) {
        if (null == input) {
            return new AlwaysMatch(dft);
        }
        if (input instanceof WnMatch) {
            return (WnMatch) input;
        }
        if (input instanceof Boolean) {
            return new AlwaysMatch((Boolean) input);
        }
        AutoMatch am = new AutoMatch(input);
        if (am.m instanceof ParallelMatch) {
            ((ParallelMatch) am.m).setDefaultMatch(dft);
        }
        return am;
    }

    public static List<WebException> matchErrors(WnMatch m, Object src) {
        // 防空
        if (null == src) {
            return new LinkedList<>();
        }
        // 集合就循环递归
        if (src instanceof Collection<?>) {
            List<WebException> errList = new ArrayList<WebException>();
            Collection<?> col = (Collection<?>) src;
            for (Object ele : col) {
                List<WebException> errors = matchErrors(m, ele);
                if (null != errors) {
                    errList.addAll(errors);
                }
            }
            return errList;
        }
        // 详细判断
        if (m instanceof MapMatch) {
            return ((MapMatch) m).matchErr(src);
        }
        // 获取错误列表
        List<WebException> errList = new ArrayList<WebException>();
        if (!m.match(src)) {
            errList.add(Er.create("e.obj.Invalid", src));
        }
        return errList;
    }

    private WnMatch m;

    public AutoMatch(Object input) {
        this.update(input);
    }

    @SuppressWarnings("unchecked")
    public AutoMatch update(Object input) {
        // null
        if (null == input) {
            this.m = new NilMatch();
        }
        // Boolean
        else if (input instanceof Boolean) {
            this.m = new BooleanMatch((Boolean) input);
        }
        // Integer
        else if (input instanceof Integer) {
            this.m = new IntegerMatch((Integer) input);
        }
        // Long
        else if (input instanceof Long) {
            this.m = new LongMatch((Long) input);
        }
        // Float
        else if (input instanceof Float) {
            this.m = new FloatMatch((Float) input);
        }
        // Double
        else if (input instanceof Double) {
            this.m = new DoubleMatch((Double) input);
        }
        // BigDecimal
        else if (input instanceof BigDecimal) {
            this.m = new BigDecimalMatch((BigDecimal) input);
        }
        // List
        else if (input instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) input;
            WnMatch[] ms = new WnMatch[coll.size()];
            int i = 0;
            for (Object o : coll) {
                ms[i++] = new AutoMatch(o);
            }
            this.m = new ParallelMatch(ms);
        }
        // Array
        else if (input.getClass().isArray()) {
            int len = Array.getLength(input);
            WnMatch[] ms = new WnMatch[len];
            int i = 0;
            for (i = 0; i < len; i++) {
                Object o = Array.get(input, i);
                ms[i] = new AutoMatch(o);
            }
            this.m = new ParallelMatch(ms);
        }
        // Map
        else if (input instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) input;
            // 搜索列表内容
            if ("findInArray".equals(map.get("matchMode")) && map.containsKey("matchBy")) {
                this.m = new MapFindInArrayMatch(map);
            }
            // 普通的 Match
            else {
                this.m = new MapMatch(map);
            }
        }
        // String
        else if (input instanceof CharSequence) {
            AutoStrMatch sm = new AutoStrMatch((CharSequence) input);
            if (sm.isNot()) {
                this.m = sm;
            } else {
                this.m = sm.getMatch();
            }
        }
        // Unsupported
        else {
            throw Er.create("e.match.unsupport", input);
        }

        return this;
    }

    @Override
    public boolean match(Object val) {
        return m.match(val);
    }

}
