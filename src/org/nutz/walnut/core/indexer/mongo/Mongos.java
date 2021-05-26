package org.nutz.walnut.core.indexer.mongo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.Document;
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
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.core.bean.WnIoObj;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wregion;

import com.mongodb.client.FindIterable;

/**
 * MongoDB 相关帮助函数集
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class Mongos {
    /**
     * @param q
     *            对象查询条件
     * @return MongoDB 的查询条件
     */
    public static ZMoDoc toQueryDoc(WnQuery q) {
        List<ZMoDoc> list = new LinkedList<>();
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

    static void _put_to_query(ZMoDoc q, boolean not, String key, Object v) {
        if (not) {
            if (null == v) {
                q.put(key, Wlang.map("$ne", v));
            } else {
                Mirror<?> mi = Mirror.me(v);
                if (mi.isSimple()) {
                    q.put(key, Wlang.map("$ne", v));
                } else {
                    q.put(key, Wlang.map("$not", v));
                }
            }
        } else {
            q.put(key, v);
        }
    }

    @SuppressWarnings("unchecked")
    static void _set_to_doc(ZMoDoc q, String key, Object v) {
        boolean not = false;
        if (key.startsWith("!")) {
            not = true;
            key = key.substring(1).trim();
        }
        if (null == v) {
            _put_to_query(q, not, key, null);
        }
        // 数字类型
        else if (v instanceof Number) {
            _put_to_query(q, not, key, v);
        }
        // 范围
        else if (v instanceof Region) {
            __set_region_to_doc(q, not, key, (Region<?>) v);
        }
        // Regex
        else if (v instanceof Pattern) {
            _put_to_query(q, not, key, v);
        }
        // Collection
        else if (v instanceof Collection) {
            Collection<Object> col = (Collection<Object>) v;
            Object[] vv = col.toArray(new Object[col.size()]);
            _set_array_to_doc(q, not, key, vv);
        }
        // Array
        else if (v.getClass().isArray()) {
            _set_array_to_doc(q, not, key, (Object[]) v);
        }
        // Map
        else if (v instanceof Map) {
            _set_map_to_doc(q, not, key, (Map<String, Object>) v);
        }
        // Simple value
        else {
            Mirror<?> mi = Mirror.me(v);
            // 字符串
            if (mi.isStringLike()) {
                String s = v.toString();
                // 非空
                if (s.length() == 0) {
                    if (not) {
                        q.eq(key, null);
                    } else {
                        q.ne(key, null);
                    }
                    return;
                }

                // 如果是范围，那么默认的，那么展开里面的内容
                s = Wregion.extend_rg_macro(s);

                // 正则表达式
                if (s.startsWith("^")) {
                    Object v2 = Pattern.compile(s);
                    _put_to_query(q, not, key, v2);
                }
                // 正则表达式取反
                else if (s.startsWith("!^")) {
                    Object v2 = Pattern.compile(s.substring(1));
                    not = !not;
                    _put_to_query(q, not, key, v2);
                }
                // 表示不等于
                else if (s.startsWith("!")) {
                    not = !not;
                    // 直接是 "!" 表示不等于 null
                    if ("!".equals(s)) {
                        _put_to_query(q, not, key, null);
                    }
                    // 否则取值
                    else {
                        String s2 = s.substring(1);
                        _put_to_query(q, not, key, s2);
                    }
                }
                // 通配符
                else if (s.contains("*")) {
                    String regex = "^" + s.replace("*", ".*");
                    q.put(key, Pattern.compile(regex));
                }
                // 整数范围
                else if (s.matches(Wregion.intRegion())) {
                    IntRegion rg = Region.Int(s);
                    __set_region_to_doc(q, not, key, rg);
                }
                // 长整数范围
                else if (s.matches(Wregion.longRegion())) {
                    LongRegion rg = Region.Long(s);
                    __set_region_to_doc(q, not, key, rg);
                }
                // 浮点范围
                else if (s.matches(Wregion.floatRegion())) {
                    FloatRegion rg = Region.Float(s);
                    __set_region_to_doc(q, not, key, rg);
                }
                // 日期范围
                else if (s.matches(Wregion.dateRegion("^[Dd]ate"))) {
                    String s2 = s.substring(4).trim();
                    s2 = Wregion.extend_rg_macro(s2);
                    DateRegion rg = Region.Date(s2);
                    __set_region_to_doc(q, not, key, rg);
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

                    __set_region_to_doc(q, not, key, rg2);
                }
                // 普通字符串
                else {
                    _put_to_query(q, not, key, s);
                }
            }
            // 其他直接搞吧
            else {
                _put_to_query(q, not, key, v);
            }
        }
    }

    private static final Pattern _P = Pattern.compile("^[$%](and|or)");

    @SuppressWarnings("unchecked")
    private static void _set_array_to_doc(ZMoDoc q, boolean not, String key, Object[] vv) {
        if (vv.length > 0) {
            Mirror<?> mi = Mirror.me(vv[0]);

            // 是联合的查询
            if (mi.isMap()) {
                Matcher m = _P.matcher(key);
                // 那么 key 必须是 $and 或者 $or
                if (m.find()) {
                    key = "$" + m.group(1);
                    ZMoDoc[] list = new ZMoDoc[vv.length];
                    // 遍历所有的查询条件
                    for (int i = 0; i < vv.length; i++) {
                        Object v2 = vv[i];
                        Map<String, Object> map = (Map<String, Object>) v2;
                        ZMoDoc doc = ZMoDoc.NEW();
                        for (Map.Entry<String, Object> en : map.entrySet()) {
                            String key2 = en.getKey();
                            Object val2 = en.getValue();
                            _set_to_doc(doc, key2, val2);
                        }
                        list[i] = doc;
                    }
                    // 重新应用一下条件
                    _put_to_query(q, not, key, list);
                }
                // 靠，错误的 Key
                else {
                    throw Er.create("e.io.query.unsuport", q.toString());
                }
            }
            // 那就作为普通元素的枚举
            else {
                _set_enum_to_doc(q, not, key, vv);
            }
        }
    }

    private static void __set_region_to_doc(ZMoDoc q, boolean not, String key, Region<?> rg) {
        // 如果是一个范围
        if (rg.isRegion()) {
            ZMoDoc doc = ZMoDoc.NEW();
            if (rg.left() != null) {
                doc.put(rg.leftOpt("$gt", "$gte"), rg.left());
            }
            if (rg.right() != null) {
                doc.put(rg.rightOpt("$lt", "$lte"), rg.right());
            }
            _put_to_query(q, not, key, doc);
        }
        // 如果是一个精确的值
        else if (!rg.isNull()) {
            // 如果两边都是开区间表示不等于
            if (rg.isLeftOpen() && rg.isRightOpen()) {
                // 不等于再 not，那就是等于咯
                if (not) {
                    q.put(key, rg.left());
                }
                // 那就是不等于
                else {
                    q.put(key, ZMoDoc.NEW("$ne", rg.left()));
                }
            }
            // 否则表示等于
            else {
                // 等于再 not，那就是不等于咯
                if (not) {
                    q.put(key, ZMoDoc.NEW("$ne", rg.left()));
                }
                // 那就是等于
                else {
                    q.put(key, rg.left());
                }
            }
        }
    }

    private static void _set_map_to_doc(ZMoDoc q,
                                        boolean not,
                                        String key,
                                        Map<String, Object> map) {
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
        _put_to_query(q, not, key, doc);
    }

    private static void _set_enum_to_doc(ZMoDoc q, boolean not, String key, Object[] ss) {
        // 指明了 in/all/nin
        if (key.matches("^[%$](n?in|all)$")) {
            _put_to_query(q, not, key, ss);
        }
        // 单个值
        else if (ss.length == 1) {
            _put_to_query(q, not, key, ss[0]);
        }
        // 多个值，看看是 “与” 还是 “或”
        else if (ss.length > 0) {
            ZMoDoc q2 = q;
            // 搞一个嵌套
            if (not) {
                q = new ZMoDoc();
            }
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
            // 指明了 or 那么全部变成条件
            else if (ss[0].equals("%or")) {
                ZMoDoc[] list = new ZMoDoc[ss.length - 1];
                for (int i = 1; i < ss.length; i++) {
                    ZMoDoc doc = ZMoDoc.NEW();
                    _set_to_doc(doc, key, ss[i]);
                    list[i - 1] = doc;
                }
                q.put("$or", list);
            }
            // 默认用 in
            else {
                q.in(key, ss);
            }
            // 嵌套进去
            if (not) {
                _put_to_query(q2, not, key, q);
            }
        }
    }

    /**
     * 设置游标的分页
     * 
     * @param it
     *            游标
     * @param q
     *            查询对象
     */
    public static void setup_paging(FindIterable<Document> it, WnQuery q) {
        if (null == q)
            return;
        if (q.limit() > 0) {
            it.limit(q.limit());
        }
        if (q.skip() > 0) {
            it.skip(q.skip());
        }
    }

    /**
     * 设置游标的排序
     * 
     * @param it
     *            迭代器
     * @param q
     *            查询对象
     */
    public static void setup_sorting(FindIterable<Document> it, WnQuery q) {
        NutMap sort = q.sort();
        if (null != sort && sort.size() > 0) {
            it.sort(ZMo.me().toDoc(sort));
        }
    }

    public static ZMoDoc qID(String id) {
        return ZMoDoc.NEW("id", id);
    }

    public static WnIoObj toWnObj(Document dbobj) {
        if (null == dbobj)
            return null;
        ZMoDoc doc = ZMoDoc.WRAP(dbobj);
        WnIoObj o = ZMo.me().fromDocToMap(doc, WnIoObj.class);
        // 这里，为了之前的程序错误（有时候吧 ph存到集合里了），强制删除一下
        if (null != o) {
            o.remove("ph");
            o.remove("_id");
        }
        return o;
    }
}
