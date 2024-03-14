package com.site0.walnut.ext.data.sqlx.ast;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.ext.data.sqlx.ast.cri.*;

public abstract class SqlCriteria {

    public static SqlCriteriaNode toCriNode(Object input) {
        // Collection
        if (input instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> col = (Collection<Object>) input;
            List<SqlCriteriaNode> nodes = new ArrayList<>(col.size());
            for (Object item : col) {
                SqlCriteriaNode node = toCriNode(item);
                nodes.add(node);
            }
            return or(nodes);
        }
        // Array
        if (input.getClass().isArray()) {
            Object[] vv = (Object[]) input;
            List<SqlCriteriaNode> nodes = new ArrayList<>(vv.length);
            for (Object item : vv) {
                SqlCriteriaNode node = toCriNode(item);
                nodes.add(node);
            }
            return or(nodes);
        }
        // Map
        // 支持一些特殊的语法 "[%$](eq|ne|gt|gte|lt|lte|in|nin)" 等
        if (input instanceof Map) {
            @SuppressWarnings("unchecked")
            NutMap map = NutMap.WRAP((Map<String, Object>) input);
            List<SqlCriteriaNode> nodes = new ArrayList<>(map.size());
            for (Map.Entry<String, Object> en : map.entrySet()) {
                String k = en.getKey();
                Object v = en.getValue();
                SqlCriteriaNode node = anyToExp(k, v);
                nodes.add(node);
            }
            return and(nodes);
        }

        // 不支持的输入
        throw Er.create("e.sql.cri.unsupportInputType", input.getClass().getSimpleName());
    }

    public static SqlCriteriaNode anyToExp(String key, Object val) {
        boolean not = false;
        if (key.startsWith("!")) {
            not = true;
            key = key.substring(1).trim();
        }
        // 准备返回值
        SqlCriteriaNode ex = null;

        // 按条件判断值，决定采用哪种表达式
        if (null == val) {
            ex = new SqlCriExpNullNode(key);
        }
        // 如果是空串
        else if (val instanceof CharSequence) {
            ex = strToExp(key, val.toString());
        }
        // 数字类型
        else if ((val instanceof Number) || (val instanceof Boolean)) {
            ex = new SqlCriExpSimpleEqNode(key, val);
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
        else if (val instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> col = (Collection<Object>) val;
            Object[] vv = col.toArray(new Object[col.size()]);
            ex = new SqlCriExpEnumNode(key, vv);
        }
        // Array
        else if (val.getClass().isArray()) {
            Object[] vv = (Object[]) val;
            ex = new SqlCriExpEnumNode(key, vv);
        }
        // Map
        // 支持一些特殊的语法 "[%$](eq|ne|gt|gte|lt|lte|in|nin)" 等
        else if (val instanceof Map) {
            @SuppressWarnings("unchecked")
            NutMap map = NutMap.WRAP((Map<String, Object>) val);
            ex = mapToExp(key, map);
        }
        // 不支持
        else {
            throw Er.create("e.sql.cri.unsupportInput", val);
        }

        if (not) {
            ex.toggleNot();
        }

        return ex;
    }

    private static final Pattern _P2 = Pattern.compile("^[$%](eq|ne|gt|gte|lt|lte|in|nin)$");

    public static SqlCriteriaNode mapToExp(String key, Map<String, Object> map) {
        List<SqlCriteriaNode> nodes = new ArrayList<>(map.size());
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String k = en.getKey();
            Matcher m = _P2.matcher(k);
            if (m.find()) {
                String mode = m.group(1);
                Object val = en.getValue();
                // $eq
                if ("eq".equals(mode)) {
                    nodes.add(new SqlCriExpSimpleEqNode(key, val));
                }
                // $ne
                else if ("ne".equals(mode)) {
                    nodes.add(new SqlCriExpSimpleEqNode(key, val).setNot(true));
                }
                // $lt
                else if ("lt".equals(mode)) {
                    nodes.add(new SqlCriExpSimpleLtNode(key, val));
                }
                // $lte
                else if ("lte".equals(mode)) {
                    nodes.add(new SqlCriExpSimpleLteNode(key, val));
                }
                // $gt
                else if ("gt".equals(mode)) {
                    nodes.add(new SqlCriExpSimpleGtNode(key, val));
                }
                // $gte
                else if ("gte".equals(mode)) {
                    nodes.add(new SqlCriExpSimpleGteNode(key, val));
                }
                // $in
                else if ("in".equals(mode)) {
                    Object[] vv = Castors.me().castTo(val, Object[].class);
                    nodes.add(new SqlCriExpEnumNode(key, vv));
                }
                // $nin
                else if ("nin".equals(mode)) {
                    Object[] vv = Castors.me().castTo(val, Object[].class);
                    nodes.add(new SqlCriExpEnumNode(key, vv).setNot(true));
                }
            }
        }
        return and(nodes);
    }

    public static SqlCriteriaNode strToExp(String name, String s) {
        // 非空
        if ("".equals(s)) {
            return new SqlCriExpNullNode(name).setNot(true);
        }

        // 正则表达式
        if (s.startsWith("^")) {
            return new SqlCriExpRegexpNode(name, s);
        }

        // 正则表达式
        if (s.startsWith("!^")) {
            String v = s.substring(1);
            return new SqlCriExpRegexpNode(name, v).setNot(true);
        }

        // 表示不等于
        if (s.startsWith("!")) {
            // 直接是 "!" 表示不等于 null
            if ("!".equals(s)) {
                return new SqlCriExpNullNode(name).setNot(true);
            }
            // 否则取值
            String v = s.substring(1);
            return new SqlCriExpSimpleEqNode(name, v).setNot(true);
        }

        // 通配符
        if (s.contains("*") || s.contains("?")) {
            String s2 = s.replace('*', '%').replace('?', '_');
            return new SqlCriExpLikeNode(name, s2);
        }

        // 范围
        SqlCriExpRangeNode rg = SqlCriExpRangeNode.tryParse(name, s);
        if (null != rg) {
            return rg;
        }

        // 简单值
        return new SqlCriExpSimpleEqNode(name, s);
    }

    public static SqlCriteriaNode and(List<SqlCriteriaNode> nodes) {
        if (null == nodes) {
            return null;
        }
        return andGroup(nodes);
    }

    public static SqlCriGroupNode andGroup(List<SqlCriteriaNode> nodes) {
        SqlCriGroupNode grp = new SqlCriGroupNode();
        if (!nodes.isEmpty()) {
            Iterator<SqlCriteriaNode> it = nodes.iterator();
            // 头节点
            grp.setHeadNode(it.next());
            SqlCriteriaNode head = grp.getHeadNode();

            // 后续节点
            while (it.hasNext()) {
                head.and(it.next());
                head = head.getNextNode();
            }
        }
        return grp;
    }

    public static SqlCriteriaNode or(List<SqlCriteriaNode> nodes) {
        if (null == nodes) {
            return null;
        }
        return orGroup(nodes);
    }

    public static SqlCriGroupNode orGroup(List<SqlCriteriaNode> nodes) {
        if (null == nodes) {
            return null;
        }
        SqlCriGroupNode grp = new SqlCriGroupNode();
        if (!nodes.isEmpty()) {
            Iterator<SqlCriteriaNode> it = nodes.iterator();
            // 头节点
            grp.setHeadNode(it.next());
            SqlCriteriaNode head = grp.getHeadNode();

            // 后续节点
            while (it.hasNext()) {
                head.or(it.next());
                head = head.getNextNode();
            }
        }
        return grp;
    }

}
