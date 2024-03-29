package org.nutz.mongo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.nutz.castor.Castors;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mongo.adaptor.ZMoAs;
import com.site0.walnut.api.err.Er;

/**
 * 包裹了 DBObject，并提供了一些更便利的方法
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZMoDoc extends Document {

    public static ZMoDoc NEW() {
        return new ZMoDoc();
    }

    public static ZMoDoc NEW(Map<String, Object> m) {
        ZMoDoc doc = NEW();
        doc.putAll(m);
        return doc;
    }

    public static ZMoDoc NEW(String key, Object v) {
        return NEW().putv(key, v);
    }

    public static ZMoDoc ID(Object id) {
        return NEW().putv("_id", id);
    }

    public static <T> ZMoDoc IN(String key, T[] vs) {
        return NEW().in(key, vs);
    }

    public static <T> ZMoDoc ALL(String key, T[] vs) {
        return NEW().all(key, vs);
    }

    public static ZMoDoc NOID() {
        return NEW().putv("_id", 0);
    }

    public static ZMoDoc NOID(String key, int v) {
        return NOID().putv(key, v);
    }

    public static ZMoDoc SET(String key, Object v) {
        return NEW().set(key, v);
    }

    public static ZMoDoc SET(Document dbo) {
        return NEW().set(dbo);
    }

    public static ZMoDoc M(String mnm, String key, Object v) {
        return NEW().m(mnm, key, v);
    }

    public static ZMoDoc NEW(String json) {
        return NEW(Wlang.map(json));
    }

    public static ZMoDoc NEWf(String jsonf, Object... args) {
        return NEW(Wlang.mapf(jsonf, args));
    }

    public static ZMoDoc WRAP(Document obj) {
        if (null == obj)
            return null;
        if (obj instanceof ZMoDoc)
            return (ZMoDoc) obj;
        ZMoDoc re = new ZMoDoc();
        re.putAll(obj);
        return re;
    }

    /**
     * 重新生成 _id
     * 
     * @return 自身以便链式赋值
     */
    public ZMoDoc genID() {
        this.put("_id", new ObjectId());
        return this;
    }

    /**
     * 删除自身的 _id 字段，以便作为一个新对象插入到数据库中
     */
    public ZMoDoc asNew() {
        this.remove("_id");
        return this;
    }

    public ZMoDoc putv(String key, Object v) {
        this.put(key, v);
        return this;
    }

    public ZMoDoc rm(String... keys) {
        for (String key : keys)
            this.remove(key);
        return this;
    }

    public <T> ZMoDoc in(String key, T[] vs) {
        this.put(key, NEW("$in", vs));
        return this;
    }

    public <T> ZMoDoc nin(String key, T[] vs) {
        this.put(key, NEW("$nin", vs));
        return this;
    }

    public <T> ZMoDoc all(String key, T[] vs) {
        this.put(key, NEW("$all", vs));
        return this;
    }

    // ------------------------------------------------------------
    // 下面是一些便捷的方法用来设置常用的值
    /**
     * 本函数会设置 "$set" : {...} ，如果没有 "$set" 键，会添加
     * 
     * @param key
     *            要设置字段的名称
     * @param v
     *            要设置字段的值
     * @return 自身以便链式赋值
     */
    public ZMoDoc set(String key, Object v) {
        return m("$set", key, v);
    }

    /**
     * 本函数会将一个字段对象变为 $set，如果已经有了 $set 则合并
     * 
     * @param dbo
     *            文档对象
     * @return 自身以便链式赋值
     */
    public ZMoDoc set(Document dbo) {
        Document o = getAs("$set", Document.class);
        if (null == o) {
            put("$set", dbo);
        } else {
            o.putAll(dbo);
        }
        return this;
    }

    /**
     * 删除一组字段。 相当于 <code>$unset : {a:1,b:1...}</code>
     * 
     * @param keys
     *            字段名
     * @return 自身以便链式赋值
     */
    public ZMoDoc unset(String... keys) {
        for (String key : keys) {
            m("$unset", key, 1);
        }
        return this;
    }

    /**
     * 本函数会设置 "mnm" : {...} ，如果没有修改器的键，会添加这个对象，如果有，合并
     * 
     * @param mnm
     *            修改器名称
     * @param key
     *            键
     * @param v
     *            值
     * @return 自身以便链式赋值
     */
    public ZMoDoc m(String mnm, String key, Object v) {
        Document o = getAs(mnm, Document.class);
        if (null == o) {
            o = ZMoDoc.NEW();
            put(mnm, o);
        }
        o.put(key, v);
        return this;
    }

    public ZMoDoc exists(String nm, boolean exists) {
        put(nm, NEW("$exists", exists));
        return this;
    }

    public ZMoDoc eq(String nm, Object v) {
        put(nm, NEW("$eq", v));
        return this;
    }

    public ZMoDoc ne(String nm, Object v) {
        put(nm, NEW("$ne", v));
        return this;
    }

    public ZMoDoc gte(String nm, Object v) {
        put(nm, NEW("$gte", v));
        return this;
    }

    public ZMoDoc gt(String nm, Object v) {
        put(nm, NEW("$gt", v));
        return this;
    }

    public ZMoDoc lte(String nm, Object v) {
        put(nm, NEW("$lte", v));
        return this;
    }

    public ZMoDoc lt(String nm, Object v) {
        put(nm, NEW("$lt", v));
        return this;
    }

    public NutMap toNutMap() {
        NutMap map = new NutMap();
        for (String key : this.keySet())
            map.put(key, this.get(key));
        return map;
    }

    // ------------------------------------------------------------
    // 下面是一些便捷的方法赖访问字段的值
    public ObjectId getId() {
        return this.getAs("_id", ObjectId.class);
    }

    public ObjectId getAsId(String key) {
        Object obj = this.get(key);
        if (null == obj)
            return null;
        if (obj instanceof ObjectId)
            return (ObjectId) obj;
        return new ObjectId(obj.toString());
    }

    public int getInt(String key) {
        return getInt(key, -1);
    }

    public int getInt(String key, int dft) {
        Object v = get(key);
        return null == v ? dft : Castors.me().castTo(v, int.class);
    }

    public float getFloat(String key) {
        return getFloat(key, Float.NaN);
    }

    public float getFloat(String key, float dft) {
        Object v = get(key);
        return null == v ? dft : Castors.me().castTo(v, float.class);
    }

    public long getLong(String key) {
        return getLong(key, -1L);
    }

    public long getLong(String key, long dft) {
        Object v = get(key);
        return null == v ? dft : Castors.me().castTo(v, long.class);
    }

    public double getDouble(String key) {
        return getDouble(key, Double.NaN);
    }

    public double getDouble(String key, double dft) {
        Object v = get(key);
        return null == v ? dft : Castors.me().castTo(v, double.class);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean dft) {
        Object v = get(key);
        return null == v ? dft : Castors.me().castTo(v, boolean.class);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String dft) {
        Object v = get(key);
        return null == v ? dft : Castors.me().castTo(v, String.class);
    }

    public Date getTime(String key) {
        return getTime(key, null);
    }

    public Date getTime(String key, Date dft) {
        Object v = get(key);
        return null == v ? dft : Castors.me().castTo(v, Date.class);
    }

    public <T extends Enum<?>> T getEnum(String key, Class<T> classOfEnum) {
        return getEnum(key, classOfEnum, null);
    }

    public <T extends Enum<?>> T getEnum(String key, Class<T> classOfEnum, T dft) {
        String s = getString(key);
        if (Strings.isBlank(s))
            return dft;
        return Castors.me().castTo(s, classOfEnum);
    }

    public boolean isEnum(String key, Enum<?>... eus) {
        if (null == eus || eus.length == 0)
            return false;
        try {
            Enum<?> v = getEnum(key, eus[0].getClass());
            for (Enum<?> eu : eus)
                if (!v.equals(eu))
                    return false;
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public <T> T getAs(String key, Class<T> classOfT) {
        return getAs(key, classOfT, null);
    }

    public <T> T getAs(String key, Class<T> classOfT, T dft) {
        Object v = get(key);
        return null == v ? dft : Castors.me().castTo(v, classOfT);
    }

    /**
     * 将一个字段转换成列表。因为返回的是容器，所以本函数永远不会返回 null
     * 
     * @param <T>
     * @param key
     * @param eleType
     * @return 列表对象，如果字段不存在或者为空，则返回一个空列表
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, final Class<T> eleType) {
        Object v = get(key);
        if (null == v)
            return new ArrayList<T>();

        if (v instanceof CharSequence) {
            return Wlang.list(Castors.me().castTo(v, eleType));
        }

        int len = Wlang.eleSize(v);
        final List<T> list = new ArrayList<T>(len);
        Wlang.eachEvenMap(v, (int index, Object ele, Object src) -> {
            list.add(Castors.me().castTo(ele, eleType));
        });

        return list;

    }

    /**
     * 将一个字段转换成数组。因为返回的是容器，所以本函数永远不会返回 null
     * 
     * @param <T>
     * @param key
     * @param eleType
     * @return 数组对象，如果字段不存在或者为空，则返回一个空数组
     */
    @SuppressWarnings("unchecked")
    public <T> T[] getArray(String key, final Class<T> eleType) {
        Object v = get(key);
        if (null == v)
            return (T[]) Array.newInstance(eleType, 0);

        if (v instanceof CharSequence) {
            return Wlang.array(Castors.me().castTo(v, eleType));
        }

        int len = Wlang.eleSize(v);
        final Object arr = Array.newInstance(eleType, len);
        final int[] i = new int[]{0};
        Wlang.eachEvenMap(v, (int index, Object ele, Object src) -> {
            Array.set(arr, i[0]++, Castors.me().castTo(ele, eleType));
        });

        return (T[]) arr;

    }

    // ------------------------------------------------------------
    // 下面都是委托方法

    public Object put(String key, Object v) {
        // 检查一下错误，防止 _id 输入错误
        if ("_id".equals(key)) {
            // 空值，表示移除
            if (v == null) {
                this.remove("_id");
                return null;
            }
            // 普通的 ID
            else if (v instanceof ObjectId) {
                super.put(key, v);
                return v;
            }
            // 如果是字符串，尝试转换
            else if (v instanceof CharSequence) {
                try {
                    ObjectId id = new ObjectId(v.toString());
                    super.put(key, id);
                    return id;
                }
                catch (Exception e) {
                    super.put(key, v); // 容忍非法的ObjectId
                }
            }
            // 如果是 boolean 或者整数表示过滤 或者 Document
            else if (v instanceof Boolean
                     || v instanceof Integer
                     || v instanceof Long
                     || v instanceof Document
                     || v instanceof Map) {
                super.put(key, v);
                return v;
            }
            // 否则不能接受
            else {
                throw Er.create("e.mongo.doc._id.invalid", v.getClass().getName());
            }
        }
        // 空值，直接压入
        else if (null == v) {
            super.put(key, null);
            return null;
        }
        // 对于 ObjectId
        else if (v instanceof ObjectId) {
            super.put(key, v);
            return v;
        }
        // 如果是枚举
        else if (v instanceof Enum) {
            super.put(key, v.toString());
            return v;
        }
        /*
         * 确定值不是空
         */
        // 直接是文档对象
        if (v instanceof Document) {
            return super.put(key, v);
        }

        // 其他情况，适配一下
        Object o = ZMoAs.smart().toMongo(null, v);
        super.put(key, o);
        return o;
    }

    public NutMap toMap() {
        return NutMap.WRAP(this);
    }

    public ZMoDoc clone() {
        ZMoDoc doc = ZMoDoc.NEW();
        for (String key : keySet())
            doc.put(key, get(key));
        return doc;
    }
}
