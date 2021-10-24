package org.nutz.walnut.util.validate.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.math.BigDecimal;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.validate.WnMatch;

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
        if (null == input) {
            return new AlwaysMatch(false);
        }
        if (input instanceof Boolean) {
            return new AlwaysMatch((Boolean) input);
        }
        return new AutoMatch(input);
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
            this.m = new MapMatch((Map<String, Object>) input);
        }
        // String
        else if (input instanceof CharSequence) {
            this.m = (new AutoStrMatch((CharSequence) input)).getMatch();
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
