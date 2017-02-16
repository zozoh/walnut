package org.nutz.walnut.impl.io.mongo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.util.DateRegion;
import org.nutz.lang.util.FloatRegion;
import org.nutz.lang.util.IntRegion;
import org.nutz.lang.util.LongRegion;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.io.WnBean;
import org.nutz.walnut.util.WnRg;

import com.mongodb.BasicDBList;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public abstract class WnMongos {

    /**
     * @param q
     *            对象查询条件
     * @return MongoDB 的查询条件
     */
    public static ZMoDoc toQueryDoc(WnQuery q) {
        BasicDBList list = new BasicDBList();
        // 遍历所有的查询条件
        for (NutMap map : q.getList()) {
            ZMoDoc doc = ZMoDoc.NEW();
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
                _set_to_doc(doc, key, val);
            }
            list.add(doc);
        }

        // 根据列表生成查询条件
        ZMoDoc qDoc = null;
        if (!list.isEmpty()) {
            // 查询是个多元素数组，则表示 or
            if (list.size() > 1) {
                qDoc = ZMoDoc.NEW();
                qDoc.put("$or", list);
            }
            // 否则就单单一个对象
            else {
                qDoc = (ZMoDoc) list.get(0);
            }
        }
        return qDoc;
    }

    @SuppressWarnings("unchecked")
    static void _set_to_doc(ZMoDoc q, String key, Object v) {
        if (null == v) {
            q.put(key, null);
        }
        // 数字类型
        else if (v instanceof Number) {
            q.put(key, v);
        }
        // 范围
        else if (v instanceof Region) {
            __set_region_to_doc(q, key, (Region<?>) v);
        }
        // Regex
        else if (v instanceof Pattern) {
            q.put(key, v);
        }
        // Collection
        else if (v instanceof Collection) {
            Collection<Object> col = (Collection<Object>) v;
            Object[] vv = col.toArray(new Object[col.size()]);
            _set_array_to_doc(q, key, vv);
        }
        // Array
        else if (v.getClass().isArray()) {
            _set_array_to_doc(q, key, (Object[]) v);
        }
        // Map
        else if (v instanceof Map) {
            _set_map_to_doc(q, key, (Map<String, Object>) v);
        }
        // Simple value
        else {
            Mirror<?> mi = Mirror.me(v);
            // 字符串
            if (mi.isStringLike()) {
                String s = v.toString();
                // 非空
                if (s.length() == 0) {
                    q.ne(key, null);
                    return;
                }

                // 如果是范围，那么默认的，那么展开里面的内容
                s = WnRg.extend_rg_macro(s);

                // 正则表达式
                if (s.startsWith("^")) {
                    q.put(key, Pattern.compile(s));
                }
                // 正则表达式取反
                else if (s.startsWith("!^")) {
                    q.put(key, Lang.map("$not", Pattern.compile(s.substring(1))));
                }
                // 表示不等于
                else if (s.startsWith("!")) {
                    // 直接是 "!" 表示不等于 null
                    if ("!".equals(s)) {
                        q.put(key, Lang.map("$ne", null));
                    }
                    // 否则取值
                    else {
                        q.put(key, Lang.map("$ne", s.substring(1)));
                    }
                }
                // 通配符
                else if (s.contains("*")) {
                    String regex = "^" + s.replace("*", ".*");
                    q.put(key, Pattern.compile(regex));
                }
                // 整数范围
                else if (s.matches(WnRg.intRegion())) {
                    IntRegion rg = Region.Int(s);
                    __set_region_to_doc(q, key, rg);
                }
                // 长整数范围
                else if (s.matches(WnRg.longRegion())) {
                    LongRegion rg = Region.Long(s);
                    __set_region_to_doc(q, key, rg);
                }
                // 浮点范围
                else if (s.matches(WnRg.floatRegion())) {
                    FloatRegion rg = Region.Float(s);
                    __set_region_to_doc(q, key, rg);
                }
                // 日期范围
                else if (s.matches(WnRg.dateRegion("^"))) {
                    DateRegion rg = Region.Date(s);
                    __set_region_to_doc(q, key, rg);
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

                    __set_region_to_doc(q, key, rg2);
                }
                // 普通字符串
                else {
                    q.put(key, s);
                }
            }
            // 其他直接搞吧
            else {
                q.put(key, v);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void _set_array_to_doc(ZMoDoc q, String key, Object[] vv) {
        if (vv.length > 0) {
            Mirror<?> mi = Mirror.me(vv[0]);

            // 是联合的查询
            if (mi.isMap()) {
                // 那么 key 必须是 $and 或者 $or
                if ("$and".equals(key) || "$or".equals(key)) {
                    BasicDBList list = new BasicDBList();
                    // 遍历所有的查询条件
                    for (Object v2 : vv) {
                        Map<String, Object> map = (Map<String, Object>) v2;
                        ZMoDoc doc = ZMoDoc.NEW();
                        for (Map.Entry<String, Object> en : map.entrySet()) {
                            String key2 = en.getKey();
                            Object val2 = en.getValue();
                            _set_to_doc(doc, key2, val2);
                        }
                        list.add(doc);
                    }
                    q.put(key, list);
                }
                // 靠，错误的 Key
                else {
                    throw Er.create("e.io.query.unsuport", q.toString());
                }
            }
            // 那就作为普通元素的枚举
            else {
                _set_enum_to_doc(q, key, vv);
            }
        }
    }

    private static void __set_region_to_doc(ZMoDoc q, String key, Region<?> rg) {
        // 如果是一个范围
        if (rg.isRegion()) {
            ZMoDoc doc = ZMoDoc.NEW();
            if (rg.left() != null) {
                doc.put(rg.leftOpt("$gt", "$gte"), rg.left());
            }
            if (rg.right() != null) {
                doc.put(rg.rightOpt("$lt", "$lte"), rg.right());
            }
            q.put(key, doc);
        }
        // 如果是一个精确的值
        else if (!rg.isNull()) {
            q.put(key, rg.left());
        }
    }

    private static void _set_map_to_doc(ZMoDoc q, String key, Map<String, Object> map) {
        ZMoDoc doc = ZMoDoc.NEW();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key2 = en.getKey();
            // 如果 key2 为 "%xxxx" 形式，比如 %exists:true 等值，统统换成 $
            if (key2.startsWith("%")) {
                key2 = "$" + key2.substring(1);
            }

            Object val2 = en.getValue();
            _set_to_doc(doc, key2, val2);
        }
        q.put(key, doc);
    }

    private static void _set_enum_to_doc(ZMoDoc q, String key, Object[] ss) {
        // 指明了 in/all/nin
        if (key.matches("^[$](n?in|all)$")) {
            q.put(key, ss);
        }
        // 单个值
        else if (ss.length == 1) {
            q.put(key, ss[0]);
        }
        // 多个值，看看是 “与” 还是 “或”
        else if (ss.length > 0) {
            // 指明 in
            if (ss[0].equals("%in")) {
                q.in(key, Arrays.copyOfRange(ss, 1, ss.length));
            }
            // 指明 all
            else if (ss[0].equals("%all")) {
                q.all(key, Arrays.copyOfRange(ss, 1, ss.length));
            }
            // 指明 nin
            else if (ss[0].equals("%nin")) {
                q.nin(key, Arrays.copyOfRange(ss, 1, ss.length));
            }
            // 默认用 in
            else {
                q.in(key, ss);
            }
        }
    }

    /**
     * 设置游标的分页
     * 
     * @param cu
     *            游标
     * @param q
     *            查询对象
     */
    public static void setup_paging(DBCursor cu, WnQuery q) {
        if (null == q)
            return;
        if (q.limit() > 0) {
            cu.limit(q.limit());
        }
        if (q.skip() > 0) {
            cu.skip(q.skip());
        }
    }

    /**
     * 设置游标的排序
     * 
     * @param cu
     *            游标
     * @param q
     *            查询对象
     */
    public static void setup_sorting(DBCursor cu, WnQuery q) {
        NutMap sort = q.sort();
        if (null != sort && sort.size() > 0) {
            cu.sort(ZMo.me().toDoc(sort));
        }
    }

    public static ZMoDoc qID(String id) {
        return ZMoDoc.NEW("id", id);
    }

    public static WnObj toWnObj(DBObject doc) {
        if (null == doc)
            return null;
        WnObj o = ZMo.me().fromDocToMap(doc, WnBean.class);
        o.unset("_id");
        return o;
    }

}
