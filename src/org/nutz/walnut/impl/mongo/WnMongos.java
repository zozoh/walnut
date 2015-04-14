package org.nutz.walnut.impl.mongo;

import java.util.Collection;
import java.util.regex.Pattern;

import org.bson.BSONObject;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import org.nutz.mongo.ZMo;
import org.nutz.mongo.ZMoDoc;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;

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
        for (String key : q.keySet()) {
            Object v = q.get(key);
            // 空值表示没有
            if (null == v) {
                list.add(ZMoDoc.NEW(key, null));
            }
            // 数字类型
            else if (v instanceof Number) {
                join_number(list, key, v);
            }
            // 范围
            else if (v instanceof Region) {
                join_region(list, key, (Region<?>) v);
            }
            // 数组
            if (v.getClass().isArray()) {
                join_str_array(list, key, q.get(key));
            }
            // 其他的当做字符串
            else {
                join_str(list, key, v);
            }
        }

        // 根据列表生成查询条件
        ZMoDoc qDoc = null;
        if (!list.isEmpty()) {
            qDoc = ZMoDoc.NEW();
            // 全是 "或"
            if (q.isOr()) {
                qDoc.put("$or", list);
            }
            // 如果是 AND，那么就全都丢到 _q 里 ...
            else {
                for (Object o : list) {
                    qDoc.putAll((BSONObject) o);
                }
            }
        }
        return qDoc;
    }

    @SuppressWarnings("unchecked")
    static void join_str(BasicDBList list, String key, Object v) {
        if (null == v)
            return;

        // Regex
        if (v instanceof Pattern) {
            list.add(ZMoDoc.NEW(key, v));

        }
        // Collection
        else if (v instanceof Collection) {
            Collection<Object> col = (Collection<Object>) v;
            String[] ss = new String[col.size()];
            int i = 0;
            for (Object o : col) {
                ss[i++] = o.toString();
            }
            join_str_enum(list, key, (String[]) ss);
        }
        // Array
        else if (v.getClass().isArray()) {
            join_str_enum(list, key, (String[]) v);
        }
        // Simple value
        else {
            list.add(ZMoDoc.NEW(key, v.toString()));
        }
    }

    static void join_number(BasicDBList list, String key, Object v) {
        if (null == v)
            return;

        // 精确值
        if (v instanceof Number) {
            list.add(ZMoDoc.NEW(key, v));
        }
        // 范围
        else if (v instanceof Region) {
            join_region(list, key, (Region<?>) v);
        }
    }

    @SuppressWarnings("unchecked")
    static void join_str_array(BasicDBList list, String key, Object v) {
        if (v == null)
            return;

        String[] ss;

        // Collection
        if (v instanceof Collection) {
            Collection<Object> col = (Collection<Object>) v;
            ss = new String[col.size()];
            int i = 0;
            for (Object o : col) {
                ss[i++] = o.toString();
            }
        }
        // Array
        else if (v.getClass().isArray()) {
            ss = (String[]) v;
        }
        // Simple value
        else {
            list.add(ZMoDoc.NEW(key, v.toString()));
            return;
        }

        // 单个值
        if (ss.length == 1) {
            list.add(ZMoDoc.NEW(key, ss[0]));
        }
        // 多个值，均需匹配
        else if (ss.length > 0) {
            list.add(ZMoDoc.NEW().all(key, ss));
        }
    }

    static void join_region(BasicDBList list, String key, Region<?> rg) {
        // 如果是一个范围
        if (rg.isRegion()) {
            ZMoDoc q = ZMoDoc.NEW();
            if (rg.left() != null) {
                q.put(rg.leftOpt("$gt", "$gte"), rg.left());
            }
            if (rg.right() != null) {
                q.put(rg.rightOpt("$lt", "$lte"), rg.right());
            }
            list.add(ZMoDoc.NEW(key, q));
        }
        // 如果是一个精确的值
        else if (!rg.isNull()) {
            list.add(ZMoDoc.NEW(key, rg.left()));
        }
    }

    static void join_str_enum(BasicDBList list, String key, String[] ss) {
        ZMoDoc r = enum_to_Doc(key, ss);
        if (null != r)
            list.add(r);
    }

    static ZMoDoc enum_to_Doc(String key, String[] ss) {
        ZMoDoc r = null;
        if (ss.length == 1) {
            r = ZMoDoc.NEW(key, ss[0]);
        } else if (ss.length > 0) {
            r = ZMoDoc.NEW().in(key, ss);
        }
        return r;
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
        if (null == q || !q.isPaging())
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
        if (sort.size() > 0) {
            cu.sort(ZMo.me().toDoc(sort));
        }
    }

    public static ZMoDoc qID(String id) {
        return ZMoDoc.NEW("id", id);
    }

    /**
     * 将一个 MongoDB 的查询记录转换成对象
     * 
     * @param doc
     *            MongoDB 的查询记录
     * @return 对象
     */
    public static MongoWnNode toWnNode(DBObject doc) {
        if (null == doc)
            return null;
        MongoWnNode nd = ZMo.me().fromDocToObj(doc, MongoWnNode.class);
        return nd;
    }

    public static WnObj toObj(DBObject doc) {
        if (null == doc)
            return null;
        WnObj o = ZMo.me().fromDocToMap(doc, WnObj.class);
        o.remove("_id");
        return o;
    }
}
