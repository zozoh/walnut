package com.site0.walnut.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.nutz.castor.Castors;
import org.nutz.castor.FailToCastObjectException;
import org.nutz.json.Json;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Encoding;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.MapKeyConvertor;
import org.nutz.lang.Mirror;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.reflect.ReflectTool;
import org.nutz.lang.stream.StringInputStream;
import org.nutz.lang.stream.StringOutputStream;
import org.nutz.lang.stream.StringWriter;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.NutType;
import org.nutz.lang.util.Regex;
import org.nutz.lang.util.SimpleContext;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.util.each.WnBreakException;
import com.site0.walnut.util.each.WnContinueException;
import com.site0.walnut.util.each.WnEachIteratee;

public class Wlang {

    /** 当前运行的 Java 虚拟机是否是在安卓环境 */
    public static final boolean isAndroid;

    static {
        boolean flag = false;
        try {
            Class.forName("android.Manifest");
            flag = true;
        }
        catch (Throwable e) {}
        isAndroid = flag;
    }

    /**
     * 判断当前系统是否为Windows
     *
     * @return true 如果当前系统为Windows系统
     */
    public static boolean isWin() {
        try {
            String os = System.getenv("OS");
            return os != null && os.indexOf("Windows") > -1;
        }
        catch (Throwable e) {
            return false;
        }
    }

    /**
     * 根据一个 Map，和给定的对象类型，创建一个新的 JAVA 对象
     *
     * @param src
     *            Map 对象
     * @param toType
     *            JAVA 对象类型
     * @return JAVA 对象
     * @throws FailToCastObjectException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T map2Object(Map<?, ?> src, Class<T> toType)
            throws FailToCastObjectException {
        if (null == toType)
            throw new FailToCastObjectException("target type is Null");
        // 类型相同
        if (toType == Map.class) {
            return (T) src;
        }
        // 也是一种 Map
        Mirror<T> mirror = Mirror.me(toType);
        if (Map.class.isAssignableFrom(toType)) {
            Map map;
            try {
                map = (Map) mirror.born();
                map.putAll(src);
                return (T) map;
            }
            catch (Exception e) {
                throw new FailToCastObjectException("target type fail to born!", unwrapThrow(e));
            }

        }
        // 数组
        if (toType.isArray()) {
            Object a = Array.newInstance(toType.getComponentType(), src.size());
            int i = 0;
            for (Object v : src.values()) {
                Array.set(a, i++, v);
            }
            return (T) a;
        }
        // List
        if (List.class == toType) {
            ArrayList<Object> list = new ArrayList<>();
            for (Object v : src.values()) {
                list.add(v);
            }
            return (T) list;
        }

        // POJO
        T obj = mirror.born();
        for (Field field : mirror.getFields()) {
            Object v = null;

            if (null == v && src.containsKey(field.getName())) {
                v = src.get(field.getName());
            }

            if (null != v) {
                // Class<?> ft = field.getType();
                // 获取泛型基类中的字段真实类型, https://github.com/nutzam/nutz/issues/1288
                Class<?> ft = ReflectTool.getGenericFieldType(toType, field);
                Mirror<?> miFld = Mirror.me(ft);
                Object vv = null;
                // 集合
                if (v instanceof Collection
                    && (ft.isArray() || Collection.class.isAssignableFrom(ft))) {
                    Collection c = (Collection) v;
                    // 集合到数组
                    if (ft.isArray()) {
                        vv = Wlang.collection2array(c, ft.getComponentType());
                    }
                    // 集合到集合
                    else {
                        // 创建
                        Collection newCol;
                        // Class eleType = Mirror.getGenericTypes(field, 0);
                        Class<?> eleType = ReflectTool.getParameterRealGenericClass(toType,
                                                                                    field.getGenericType(),
                                                                                    0);
                        if (ft == List.class) {
                            newCol = new ArrayList(c.size());
                        } else if (ft == Set.class) {
                            newCol = new LinkedHashSet();
                        } else {
                            try {
                                newCol = (Collection) miFld.born();
                            }
                            catch (Exception e) {
                                throw Wlang.wrapThrow(e);
                            }
                        }
                        // 赋值
                        for (Object ele : c) {
                            newCol.add(Castors.me().castTo(ele, eleType));
                        }
                        vv = newCol;
                    }
                }
                // Map
                else if (v instanceof Map && Map.class.isAssignableFrom(ft)) {
                    // 创建
                    final Map map;
                    // Map 接口
                    if (ft == Map.class) {
                        map = new NutMap();
                    }
                    // 自己特殊的 Map
                    else {
                        try {
                            map = (Map) miFld.born();
                        }
                        catch (Exception e) {
                            throw new FailToCastObjectException("target type fail to born!", e);
                        }
                    }
                    // 赋值
                    // final Class<?> valType = Mirror.getGenericTypes(field,
                    // 1);
                    // map的key和value字段类型
                    final Class<?> keyType = ReflectTool.getParameterRealGenericClass(toType,
                                                                                      field.getGenericType(),
                                                                                      0);
                    final Class<?> valType = ReflectTool.getParameterRealGenericClass(toType,
                                                                                      field.getGenericType(),
                                                                                      1);

                    Map vMap = (Map) v;
                    for (Object eno : vMap.entrySet()) {
                        Entry en = (Map.Entry) eno;
                        Object ek = Castors.me().castTo(en.getKey(), keyType);
                        Object ev = Castors.me().castTo(en.getValue(), valType);
                        map.put(ek, ev);
                    }
                    vv = map;
                }
                // 强制转换
                else {
                    vv = Castors.me().castTo(v, ft);
                }
                mirror.setValue(obj, field, vv);
            }
        }
        return obj;
    }

    /**
     * 将数组转换成另外一种类型的数组。将会采用 Castor 来深层转换数组元素
     *
     * @param array
     *            原始数组
     * @param eleType
     *            新数组的元素类型
     * @return 新数组
     * @throws FailToCastObjectException
     *
     * @see org.nutz.castor.Castors
     */
    public static Object array2array(Object array, Class<?> eleType)
            throws FailToCastObjectException {
        if (null == array)
            return null;
        int len = Array.getLength(array);
        Object re = Array.newInstance(eleType, len);
        for (int i = 0; i < len; i++) {
            Array.set(re, i, Castors.me().castTo(Array.get(array, i), eleType));
        }
        return re;
    }

    /**
     * 将数组转换成Object[] 数组。将会采用 Castor 来深层转换数组元素
     *
     * @param args
     *            原始数组
     * @param pts
     *            新数组的元素类型
     * @return 新数组
     * @throws FailToCastObjectException
     *
     * @see org.nutz.castor.Castors
     */
    public static <T> Object[] array2ObjectArray(T[] args, Class<?>[] pts)
            throws FailToCastObjectException {
        if (null == args)
            return null;
        Object[] newArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            newArgs[i] = Castors.me().castTo(args[i], pts[i]);
        }
        return newArgs;
    }

    /**
     * 将一个数组变成 Map
     *
     * @param mapClass
     *            Map 的类型
     * @param array
     *            数组
     * @param keyFieldName
     *            采用集合中元素的哪个一个字段为键。
     * @return Map 对象
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends Map> T array2map(Class<T> mapClass,
                                              Object array,
                                              String keyFieldName) {
        if (null == array)
            return null;
        T map = createMap(mapClass);
        int len = Array.getLength(array);
        if (len > 0) {
            Object obj = Array.get(array, 0);
            Mirror<?> mirror = Mirror.me(obj.getClass());
            for (int i = 0; i < len; i++) {
                obj = Array.get(array, i);
                Object key = mirror.getValue(obj, keyFieldName);
                map.put(key, obj);
            }
        }
        return map;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends Map> T array2map(Class<T> mapClass, Object array) {
        if (null == array)
            return null;
        T map = createMap(mapClass);
        int len = Array.getLength(array);
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Object key = Array.get(array, i++);
                Object val = i < len ? Array.get(array, i) : null;
                map.put(key, val);
            }
        }
        return map;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Map> T createMap(Class<T> mapClass) {
        T map;
        try {
            map = mapClass.getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {
            map = (T) new HashMap<Object, Object>();
        }
        if (!mapClass.isAssignableFrom(map.getClass())) {
            throw Wlang.makeThrow("Fail to create map [%s]", mapClass.getName());
        }
        return map;
    }

    /**
     * 将一个集合变成 Map。
     *
     * @param mapClass
     *            Map 的类型
     * @param coll
     *            集合对象
     * @param keyFieldName
     *            采用集合中元素的哪个一个字段为键。
     * @return Map 对象
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends Map> T collection2map(Class<T> mapClass,
                                                   Collection<?> coll,
                                                   String keyFieldName) {
        if (null == coll)
            return null;
        T map = createMap(mapClass);
        if (coll.size() > 0) {
            Iterator<?> it = coll.iterator();
            Object obj = it.next();
            Mirror<?> mirror = Mirror.me(obj.getClass());
            Object key = mirror.getValue(obj, keyFieldName);
            map.put(key, obj);
            for (; it.hasNext();) {
                obj = it.next();
                key = mirror.getValue(obj, keyFieldName);
                map.put(key, obj);
            }
        }
        return map;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends Map> T collection2map(Class<T> mapClass, Collection<?> coll) {
        if (null == coll)
            return null;
        T map = createMap(mapClass);
        if (coll.size() > 0) {
            Iterator<?> it = coll.iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object val = it.hasNext() ? it.next() : null;
                map.put(key, val);
            }
        }
        return map;
    }

    /**
     * 将一个集合变成 Map。
     *
     * @param mapClass
     *            Map 的类型
     * @param coll
     *            集合对象
     * @param keyFieldName
     *            采用集合中元素的哪个一个字段为键。
     * @return Map 对象
     */
    public static NutMap collection2map(Collection<?> coll, String keyFieldName) {
        if (null == coll)
            return null;
        NutMap map = new NutMap();
        if (coll.size() > 0) {
            Iterator<?> it = coll.iterator();
            Object obj = it.next();
            Mirror<?> mirror = Mirror.me(obj.getClass());
            Object key = mirror.getValue(obj, keyFieldName);
            map.put(key.toString(), obj);
            for (; it.hasNext();) {
                obj = it.next();
                key = mirror.getValue(obj, keyFieldName);
                map.put(key.toString(), obj);
            }
        }
        return map;
    }

    /**
     * 将数组转换成一个列表。
     *
     * @param array
     *            原始数组
     * @return 新列表
     *
     * @see org.nutz.castor.Castors
     */
    public static <T> List<T> array2list(T[] array) {
        if (null == array)
            return null;
        List<T> re = new ArrayList<T>(array.length);
        for (T obj : array)
            re.add(obj);
        return re;
    }

    public static Context context() {
        return new SimpleContext();
    }

    public static Context context(Map<String, Object> map) {
        return new SimpleContext(map);
    }

    public static Context context(String key, Object val) {
        SimpleContext c = new SimpleContext();
        c.set(key, val);
        return c;
    }

    public static Context context(String json) {
        NutMap map = NutMap.WRAP(json);
        return context(map);
    }

    /**
     * 返回一个 Type 的泛型数组, 如果没有, 则直接返回null
     *
     * @param type
     *            类型
     * @return 一个 Type 的泛型数组, 如果没有, 则直接返回null
     */
    public static Type[] getGenericsTypes(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return pt.getActualTypeArguments();
        }
        return null;
    }

    /**
     * 强制从字符串转换成一个 Class，将 ClassNotFoundException 包裹成 RuntimeException
     *
     * @param <T>
     * @param name
     *            类名
     * @param type
     *            这个类型的边界
     * @return 类对象
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> forName(String name, Class<T> type) {
        Class<?> re;
        try {
            re = loadClass(name);
            return (Class<T>) re;
        }
        catch (ClassNotFoundException e) {
            throw wrapThrow(e);
        }
    }

    /**
     * 将数组转换成一个列表。将会采用 Castor 来深层转换数组元素
     *
     * @param array
     *            原始数组
     * @param eleType
     *            新列表的元素类型
     * @return 新列表
     *
     * @see org.nutz.castor.Castors
     */
    public static <T, E> List<E> array2list(Object array, Class<E> eleType) {
        if (null == array)
            return null;
        int len = Array.getLength(array);
        List<E> re = new ArrayList<E>(len);
        for (int i = 0; i < len; i++) {
            Object obj = Array.get(array, i);
            re.add(Castors.me().castTo(obj, eleType));
        }
        return re;
    }

    /**
     * 将一个抛出对象的异常堆栈，显示成一个字符串
     *
     * @param e
     *            抛出对象
     * @return 异常堆栈文本
     */
    public static String getStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        StringOutputStream sbo = new StringOutputStream(sb);
        PrintStream ps = new PrintStream(sbo);
        e.printStackTrace(ps);
        ps.flush();
        return sbo.getStringBuilder().toString();
    }

    /**
     * 将枚举对象，变成集合
     *
     * @param enums
     *            枚举对象
     * @param cols
     *            集合对象
     * @return 集合对象
     */
    public static <T extends Collection<E>, E> T enum2collection(Enumeration<E> enums, T cols) {
        while (enums.hasMoreElements())
            cols.add(enums.nextElement());
        return cols;
    }

    /**
     * 将字符串解析成 boolean 值，支持更多的字符串
     * <ul>
     * <li>1 | 0
     * <li>yes | no
     * <li>on | off
     * <li>true | false
     * </ul>
     *
     * @param s
     *            字符串
     * @return 布尔值
     */
    public static boolean parseBoolean(String s) {
        if (null == s || s.length() == 0)
            return false;
        if (s.length() > 5)
            return true;
        if ("0".equals(s))
            return false;
        s = s.toLowerCase();
        return !"false".equals(s) && !"off".equals(s) && !"no".equals(s);
    }

    /**
     * 将集合变成数组，数组的类型为集合的第一个元素的类型。如果集合为空，则返回 null
     *
     * @param coll
     *            集合对象
     * @return 数组
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] collection2array(Collection<E> coll) {
        if (null == coll)
            return null;
        if (coll.size() == 0)
            return (E[]) new Object[0];

        Class<E> eleType = (Class<E>) Wlang.first(coll).getClass();
        return collection2array(coll, eleType);
    }

    /**
     * 将集合变成指定类型的数组
     *
     * @param col
     *            集合对象
     * @param eleType
     *            数组元素类型
     * @return 数组
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] collection2array(Collection<?> col, Class<E> eleType) {
        if (null == col)
            return null;
        Object re = Array.newInstance(eleType, col.size());
        int i = 0;
        for (Iterator<?> it = col.iterator(); it.hasNext();) {
            Object obj = it.next();
            if (null == obj)
                Array.set(re, i++, null);
            else
                Array.set(re, i++, Castors.me().castTo(obj, eleType));
        }
        return (E[]) re;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Map<String, Object>> void obj2map(Object obj,
                                                                T map,
                                                                final Map<Object, Object> memo) {
        // 已经转换过了，不要递归转换
        if (null == obj || memo.containsKey(obj))
            return;
        memo.put(obj, "");

        // Fix issue #497
        // 如果是 Map，就直接 putAll 一下咯
        if (obj instanceof Map<?, ?>) {
            map.putAll(__change_map_to_nutmap((Map<String, Object>) obj, memo));
            return;
        }

        // 下面是普通的 POJO
        Mirror<?> mirror = Mirror.me(obj.getClass());
        Field[] flds = mirror.getFields();
        for (Field fld : flds) {
            Object v = mirror.getValue(obj, fld);
            if (null == v) {
                continue;
            }
            Mirror<?> mr = Mirror.me(v);
            // 普通值
            if (mr.isSimple()) {
                map.put(fld.getName(), v);
            }
            // 已经输出过了
            else if (memo.containsKey(v)) {
                map.put(fld.getName(), null);
            }
            // 数组或者集合
            else if (mr.isColl()) {
                final List<Object> list = new ArrayList<Object>(Wlang.eleSize(v));
                Wlang.each(v, (int index, Object ele, Object src) -> {
                    __join_ele_to_list_as_map(list, ele, memo);
                });
                map.put(fld.getName(), list);
            }
            // Map
            else if (mr.isMap()) {
                NutMap map2 = __change_map_to_nutmap((Map<String, Object>) v, memo);
                map.put(fld.getName(), map2);
            }
            // 看来要递归
            else {
                T sub;
                try {
                    sub = (T) map.getClass().getDeclaredConstructor().newInstance();
                }
                catch (Exception e) {
                    throw Wlang.wrapThrow(e);
                }
                obj2map(v, sub, memo);
                map.put(fld.getName(), sub);
            }
        }

    }

    @SuppressWarnings("unchecked")
    private static NutMap __change_map_to_nutmap(Map<String, Object> map,
                                                 final Map<Object, Object> memo) {
        NutMap re = new NutMap();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            Object v = en.getValue();
            if (null == v)
                continue;
            Mirror<?> mr = Mirror.me(v);
            // 普通值
            if (mr.isSimple()) {
                re.put(en.getKey(), v);
            }
            // 已经输出过了
            else if (memo.containsKey(v)) {
                continue;
            }
            // 数组或者集合
            else if (mr.isColl()) {
                final List<Object> list2 = new ArrayList<Object>(Wlang.eleSize(v));
                Wlang.each(v, (int index, Object ele, Object src) -> {
                    __join_ele_to_list_as_map(list2, ele, memo);
                });
                re.put(en.getKey(), list2);
            }
            // Map
            else if (mr.isMap()) {
                NutMap map2 = __change_map_to_nutmap((Map<String, Object>) v, memo);
                re.put(en.getKey(), map2);
            }
            // 看来要递归
            else {
                NutMap map2 = obj2nutmap(v);
                re.put(en.getKey(), map2);
            }
        }
        return re;
    }

    @SuppressWarnings("unchecked")
    private static void __join_ele_to_list_as_map(List<Object> list,
                                                  Object o,
                                                  final Map<Object, Object> memo) {
        if (null == o) {
            return;
        }

        // 如果是 Map，就直接 putAll 一下咯
        if (o instanceof Map<?, ?>) {
            NutMap map2 = __change_map_to_nutmap((Map<String, Object>) o, memo);
            list.add(map2);
            return;
        }

        Mirror<?> mr = Mirror.me(o);
        // 普通值
        if (mr.isSimple()) {
            list.add(o);
        }
        // 已经输出过了
        else if (memo.containsKey(o)) {
            list.add(null);
        }
        // 数组或者集合
        else if (mr.isColl()) {
            final List<Object> list2 = new ArrayList<Object>(Wlang.eleSize(o));
            Wlang.each(o, (int index, Object ele, Object src) -> {
                __join_ele_to_list_as_map(list2, ele, memo);
            });
            list.add(list2);
        }
        // Map
        else if (mr.isMap()) {
            NutMap map2 = __change_map_to_nutmap((Map<String, Object>) o, memo);
            list.add(map2);
        }
        // 看来要递归
        else {
            NutMap map = obj2nutmap(o);
            list.add(map);
        }
    }

    /**
     * 将对象转换成 Map
     *
     * @param obj
     *            POJO 对象
     * @return Map 对象
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> obj2map(Object obj) {
        return obj2map(obj, HashMap.class);
    }

    /**
     * 将对象转为 Nutz 的标准 Map 封装
     *
     * @param obj
     *            POJO du对象
     * @return NutMap 对象
     */
    public static NutMap obj2nutmap(Object obj) {
        return obj2map(obj, NutMap.class);
    }

    /**
     * 将对象转换成 Map
     *
     * @param <T>
     * @param obj
     *            POJO 对象
     * @param mapType
     *            Map 的类型
     * @return Map 对象
     */
    public static <T extends Map<String, Object>> T obj2map(Object obj, Class<T> mapType) {
        try {
            T map = mapType.getDeclaredConstructor().newInstance();
            Wlang.obj2map(obj, map, new HashMap<Object, Object>());
            return map;
        }
        catch (Exception e) {
            throw Wlang.wrapThrow(e);
        }
    }

    /**
     * 返回一个集合对象的枚举对象。实际上就是对 Iterator 接口的一个封装
     *
     * @param col
     *            集合对象
     * @return 枚举对象
     */
    public static <T> Enumeration<T> enumeration(Collection<T> col) {
        final Iterator<T> it = col.iterator();
        return new Enumeration<T>() {
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            public T nextElement() {
                return it.next();
            }
        };
    }

    /**
     * 判断一个数组内是否包括某一个对象。 它的比较将通过 equals(Object,Object) 方法
     *
     * @param array
     *            数组
     * @param ele
     *            对象
     * @return true 包含 false 不包含
     */
    public static <T> boolean contains(T[] array, T ele) {
        if (null == array)
            return false;
        for (T e : array) {
            if (isEqual(e, ele))
                return true;
        }
        return false;
    }

    public static String concat(Collection<?> col) {
        return Ws.join(col, ",");
    }

    public static String concat(String sep, Collection<?> col) {
        return Ws.join(col, sep);
    }

    /**
     * 将一个数组的部分元素转换成字符串
     * <p>
     * 每个元素之间，都会用一个给定的字符分隔
     *
     * @param offset
     *            开始元素的下标
     * @param len
     *            元素数量
     * @param c
     *            分隔符
     * @param objs
     *            数组
     * @return 拼合后的字符串
     */
    public static <T> StringBuilder concat(int offset, int len, Object c, T[] objs) {
        StringBuilder sb = new StringBuilder();
        if (null == objs || len < 0 || 0 == objs.length)
            return sb;

        if (offset < objs.length) {
            sb.append(objs[offset]);
            for (int i = 1; i < len && i + offset < objs.length; i++) {
                sb.append(c).append(objs[i + offset]);
            }
        }
        return sb;
    }

    public static <T> String concat(T[] vals) {
        return Ws.join(vals, ",");
    }

    public static <T> String concat(String sep, T[] vals) {
        return Ws.join(vals, sep);
    }

    /**
     * 将一个数组转换成字符串
     * <p>
     * 所有的元素都被格式化字符串包裹。 这个格式话字符串只能有一个占位符， %s, %d 等，均可，请视你的数组内容而定
     * <p>
     * 每个元素之间，都会用一个给定的字符分隔
     *
     * @param ptn
     *            格式
     * @param c
     *            分隔符
     * @param objs
     *            数组
     * @return 拼合后的字符串
     */
    public static <T> StringBuilder concatBy(String ptn, Object c, T[] objs) {
        StringBuilder sb = new StringBuilder();
        for (T obj : objs)
            sb.append(String.format(ptn, obj)).append(c);
        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
        return sb;
    }

    /**
     * 获取基本类型的默认值
     *
     * @param pClass
     *            基本类型
     * @return 0/false,如果传入的pClass不是基本类型的类,则返回null
     */
    public static Object getPrimitiveDefaultValue(Class<?> pClass) {
        if (int.class.equals(pClass))
            return Integer.valueOf(0);
        if (long.class.equals(pClass))
            return Long.valueOf(0);
        if (short.class.equals(pClass))
            return Short.valueOf((short) 0);
        if (float.class.equals(pClass))
            return Float.valueOf(0f);
        if (double.class.equals(pClass))
            return Double.valueOf(0);
        if (byte.class.equals(pClass))
            return Byte.valueOf((byte) 0);
        if (char.class.equals(pClass))
            return Character.valueOf((char) 0);
        if (boolean.class.equals(pClass))
            return Boolean.FALSE;
        return null;
    }

    /**
     * 当一个类使用<T,K>来定义泛型时,本方法返回类的一个字段的具体类型。
     *
     * @param me
     * @param field
     */
    public static Type getFieldType(Mirror<?> me, String field) throws NoSuchFieldException {
        return getFieldType(me, me.getField(field));
    }

    /**
     * 当一个类使用<T, K> 来定义泛型时, 本方法返回类的一个方法所有参数的具体类型
     *
     * @param me
     * @param method
     */
    public static Type[] getMethodParamTypes(Mirror<?> me, Method method) {
        Type[] types = method.getGenericParameterTypes();
        List<Type> ts = new ArrayList<Type>();
        for (Type type : types) {
            ts.add(getGenericsType(me, type));
        }
        return ts.toArray(new Type[ts.size()]);
    }

    /**
     * 当一个类使用<T,K>来定义泛型时,本方法返回类的一个字段的具体类型。
     *
     * @param me
     * @param field
     */
    public static Type getFieldType(Mirror<?> me, Field field) {
        Type type = field.getGenericType();
        return getGenericsType(me, type);
    }

    /**
     * 当一个类使用<T,K>来定义泛型时,本方法返回类的一个字段的具体类型。
     *
     * @param me
     * @param type
     */
    public static Type getGenericsType(Mirror<?> me, Type type) {
        Type[] types = me.getGenericsTypes();
        Type t = type;
        if (type instanceof TypeVariable && types != null && types.length > 0) {
            Type[] tvs = me.getType().getTypeParameters();
            for (int i = 0; i < tvs.length; i++) {
                if (type.equals(tvs[i])) {
                    type = me.getGenericsType(i);
                    break;
                }
            }
        }
        if (!type.equals(t)) {
            return type;
        }
        if (types != null && types.length > 0 && type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;

            if (pt.getActualTypeArguments().length >= 0) {
                NutType nt = new NutType();
                nt.setOwnerType(pt.getOwnerType());
                nt.setRawType(pt.getRawType());
                Type[] tt = new Type[pt.getActualTypeArguments().length];
                for (int i = 0; i < tt.length; i++) {
                    tt[i] = types[i];
                }
                nt.setActualTypeArguments(tt);
                return nt;
            }
        }

        return type;
    }

    /**
     * 获取一个 Type 类型实际对应的Class
     *
     * @param type
     *            类型
     * @return 与Type类型实际对应的Class
     */
    @SuppressWarnings("rawtypes")
    public static Class<?> getTypeClass(Type type) {
        Class<?> clazz = null;
        if (type instanceof Class<?>) {
            clazz = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            clazz = (Class<?>) pt.getRawType();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Class<?> typeClass = getTypeClass(gat.getGenericComponentType());
            return Array.newInstance(typeClass, 0).getClass();
        } else if (type instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) type;
            Type[] ts = tv.getBounds();
            if (ts != null && ts.length > 0)
                return getTypeClass(ts[0]);
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            Type[] t_low = wt.getLowerBounds();// 取其下界
            if (t_low.length > 0)
                return getTypeClass(t_low[0]);
            Type[] t_up = wt.getUpperBounds(); // 没有下界?取其上界
            return getTypeClass(t_up[0]);// 最起码有Object作为上界
        }
        return clazz;
    }

    /**
     * 将一个 Map 所有的键都按照回调进行修改
     *
     * 本函数遇到数组或者集合，会自动处理每个元素
     *
     * @param obj
     *            要转换的 Map 或者 集合或者数组
     *
     * @param mkc
     *            键值修改的回调
     * @param recur
     *            遇到 Map 是否递归
     *
     * @see MapKeyConvertor
     */
    @SuppressWarnings("unchecked")
    public static void convertMapKey(Object obj, MapKeyConvertor mkc, boolean recur) {
        // Map
        if (obj instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>) obj;
            NutMap map2 = new NutMap();
            for (Map.Entry<String, Object> en : map.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();

                if (recur)
                    convertMapKey(val, mkc, recur);

                String newKey = mkc.convertKey(key);
                map2.put(newKey, val);
            }
            map.clear();
            map.putAll(map2);
        }
        // Collection
        else if (obj instanceof Collection<?>) {
            for (Object ele : (Collection<?>) obj) {
                convertMapKey(ele, mkc, recur);
            }
        }
        // Array
        else if (obj.getClass().isArray()) {
            for (Object ele : (Object[]) obj) {
                convertMapKey(ele, mkc, recur);
            }
        }
    }

    /**
     * 根据一段字符串，生成一个 Map 对象。
     *
     * @param str
     *            参照 JSON 标准的字符串，但是可以没有前后的大括号
     * @return Map 对象
     */
    public static NutMap map(String str) {
        if (null == str)
            return null;
        str = Strings.trim(str);
        if (!Strings.isEmpty(str)
            && (Strings.isQuoteBy(str, '{', '}') || Strings.isQuoteBy(str, '(', ')'))) {
            return Json.fromJson(NutMap.class, str);
        }
        return Json.fromJson(NutMap.class, "{" + str + "}");
    }

    /**
     * 创建一个一个键的 Map 对象
     *
     * @param key
     *            键
     * @param v
     *            值
     * @return Map 对象
     */
    public static NutMap map(String key, Object v) {
        return new NutMap().addv(key, v);
    }

    /**
     * 根据一个格式化字符串，生成 Map 对象
     *
     * @param fmt
     *            格式化字符串
     * @param args
     *            字符串参数
     * @return Map 对象
     */
    public static NutMap mapf(String fmt, Object... args) {
        return map(String.format(fmt, args));
    }

    /**
     * 自动处理对象，如果输入是个字符串，尝试自动补全{} 的JSON转换，否则就直接返回
     * 
     * @param input
     *            输入
     * @return 对象
     */
    public static Object anyToObj(Object input) {
        if (null == input) {
            return input;
        }
        if (input instanceof String) {
            String str = input.toString();
            if (Ws.isQuoteBy(str, '{', '}') || Ws.isQuoteBy(str, '[', ']')) {
                input = Json.fromJson(str);
            } else {
                input = Wlang.map(str);
            }

        }
        return input;
    }

    public static <T> List<T> anyToList(Object input, Class<T> classOfT) {
        if (null == input) {
            return null;
        }
        int N = Wlang.eleSize(input);
        List<T> list = new ArrayList<>(N);
        Wlang.each(input, (index, ele, src) -> {
            T o = Castors.me().castTo(ele, classOfT);
            list.add(o);
        });
        return list;
    }

    /**
     * 自动处理对象，如果输入是个字符串，尝试自动补全{} 的JSON转换，否则就直接返回
     * 
     * @param input
     *            输入
     * @return 对象
     */
    @SuppressWarnings("unchecked")
    public static NutMap anyToMap(Object input) {
        if (null == input) {
            return null;
        }
        if (input instanceof String) {
            return Wlang.map(input.toString());
        }
        if (input instanceof Map) {
            return NutMap.WRAP((Map<String, Object>) input);
        }
        if (input instanceof Collection) {
            return Wlang.collection2map(NutMap.class, (Collection<?>) input);
        }
        String json = Json.toJson(input);
        return Json.fromJson(NutMap.class, json);
    }

    /**
     * 获得访问者的IP地址, 反向代理过的也可以获得
     *
     * @param req
     *            请求的req对象
     * @param checkStrictBlank
     *            如果是空字符串或者不是 ipv4 或者 v6 那么就返回空串
     * @return 来源ip
     */
    public static String getIP(HttpServletRequest req, boolean checkStrictBlank) {
        if (req == null)
            return "";
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = req.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = req.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = req.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = req.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = req.getRemoteAddr();
            }
        } else if (ip.length() > 15) {
            String[] ips = ip.split(",");
            for (int index = 0; index < ips.length; index++) {
                String strIp = ips[index];
                if (!("unknown".equalsIgnoreCase(strIp))) {
                    ip = strIp;
                    break;
                }
            }
        }
        if (!checkStrictBlank) {
            return Ws.trim(ip);
        }
        if (Strings.isBlank(ip))
            return "";
        if (isIPv4Address(ip) || isIPv6Address(ip)) {
            return ip;
        }
        return "";
    }

    public static String getIP(HttpServletRequest req) {
        return getIP(req, true);
    }

    /**
     * @return 返回当前程序运行的根目录
     */
    public static String runRootPath() {
        String cp = Wlang.class.getClassLoader().getResource("").toExternalForm();
        if (cp.startsWith("file:")) {
            cp = cp.substring("file:".length());
        }
        return cp;
    }

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    private static final Pattern IPV6_STD_PATTERN = Pattern.compile("^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$");

    public static boolean isIPv4Address(final String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6StdAddress(final String input) {
        return IPV6_STD_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6HexCompressedAddress(final String input) {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(input).matches();
    }

    public static boolean isIPv6Address(final String input) {
        return isIPv6StdAddress(input) || isIPv6HexCompressedAddress(input);
    }

    /**
     * 将指定的数组的内容倒序排序。注意，这会破坏原数组的内容
     *
     * @param arrays
     *            指定的数组
     */
    public static <T> void reverse(T[] arrays) {
        int size = arrays.length;
        for (int i = 0; i < size; i++) {
            int ih = i;
            int it = size - 1 - i;
            if (ih == it || ih > it) {
                break;
            }
            T ah = arrays[ih];
            T swap = arrays[it];
            arrays[ih] = swap;
            arrays[it] = ah;
        }
    }

    /**
     * 遍历一个对象，与 each 不同，它对 Map 也会迭代
     *
     * @param obj
     *            对象
     * @param iteratee
     *            迭代器
     */
    @SuppressWarnings({"unchecked"})
    public static <T> void eachEvenMap(Object obj, WnEachIteratee<T> iteratee) {
        if (null != obj && obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            int i = 0;
            Class<T> eType = Mirror.getTypeParam(iteratee.getClass(), 0);
            if (null != eType && eType != Object.class && eType.isAssignableFrom(Entry.class)) {
                for (Object v : map.entrySet())
                    try {
                        iteratee.invoke(i++, (T) v, map);
                    }
                    catch (ContinueLoop e) {}
                    catch (ExitLoop e) {
                        break;
                    }
                    catch (WnContinueException e) {}
                    catch (WnBreakException e) {
                        break;
                    }

            } else {
                for (Map.Entry<?, ?> en : map.entrySet())
                    try {
                        Object v = en.getValue();
                        iteratee.invoke(i++, (T) v, map);
                    }
                    catch (ContinueLoop e) {}
                    catch (ExitLoop e) {
                        break;
                    }
                    catch (WnContinueException e) {}
                    catch (WnBreakException e) {
                        break;
                    }
            }
        } else {
            each(obj, iteratee);
        }
    }

    /**
     * 遍历一个对象，可以支持:
     * <ul>
     * <li>数组
     * <li>集合
     * <li>其他对象
     * </ul>
     *
     * @param obj
     *            对象
     * @param iteratee
     *            迭代器
     */
    @SuppressWarnings("unchecked")
    public static <T> void each(Object obj, WnEachIteratee<T> iteratee) {
        if (null == obj || null == iteratee)
            return;
        try {
            // 数组
            if (obj.getClass().isArray()) {
                int len = Array.getLength(obj);
                for (int i = 0; i < len; i++) {
                    T v = (T) Array.get(obj, i);
                    try {
                        iteratee.invoke(i, v, obj);
                    }
                    catch (WnContinueException e) {}
                    catch (WnBreakException e) {
                        break;
                    }
                }
            }
            // 集合
            else if (obj instanceof Collection) {
                Collection<T> col = (Collection<T>) obj;
                Iterator<T> it = col.iterator();
                int i = 0;
                while (it.hasNext()) {
                    try {
                        T v = it.next();
                        iteratee.invoke(i++, v, obj);
                    }
                    catch (WnContinueException e) {}
                    catch (WnBreakException e) {
                        break;
                    }
                }
            }
            // 迭代器
            else if (obj instanceof Iterator<?>) {
                Iterator<T> it = (Iterator<T>) obj;
                int i = 0;
                while (it.hasNext()) {
                    try {
                        T v = it.next();
                        iteratee.invoke(i++, v, obj);
                    }
                    catch (WnContinueException e) {}
                    catch (WnBreakException e) {
                        break;
                    }
                }
            }
            // 就是一个对象咯
            else {
                try {
                    T v = (T) obj;
                    iteratee.invoke(0, v, obj);
                }
                catch (WnContinueException e) {}
                catch (WnBreakException e) {}
            }
        }
        catch (WnLoopException e) {
            throw wrapThrow(e.getCause());
        }
    }

    public static int count(Object obj) {
        if (null == obj)
            return 0;

        // 数组
        if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        // 集合
        else if (obj instanceof Collection) {
            Collection<?> col = (Collection<?>) obj;
            return col.size();
        }
        // 就是一个对象咯
        return 1;
    }

    public static int toInt(Object any) {
        if (null == any) {
            return 0;
        }
        if (any instanceof Number) {
            Number a = (Number) any;
            return a.intValue();
        }
        if (any instanceof Boolean) {
            return (Boolean) any ? 1 : 0;
        }
        return count(any);
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromBytes(byte[] buf, Class<T> klass) {
        try {
            return (T) new ObjectInputStream(new ByteArrayInputStream(buf)).readObject();
        }
        catch (ClassNotFoundException e) {
            return null;
        }
        catch (IOException e) {
            return null;
        }
    }

    public static byte[] toBytes(Object obj) {
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(obj);
            return bao.toByteArray();
        }
        catch (IOException e) {
            return null;
        }
    }

    /**
     * 清除数组中的特定值
     *
     * @param objs
     *            数组
     * @param val
     *            值，可以是 null，如果是对象，则会用 equals 来比较
     * @return 新的数组实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] without(T[] objs, T val) {
        if (null == objs || objs.length == 0) {
            return objs;
        }
        List<T> list = new ArrayList<T>(objs.length);
        Class<?> eleType = null;
        for (T obj : objs) {
            if (obj == val || (null != obj && null != val && obj.equals(val)))
                continue;
            if (null == eleType && obj != null)
                eleType = obj.getClass();
            list.add(obj);
        }
        if (list.isEmpty()) {
            return (T[]) new Object[0];
        }
        return list.toArray((T[]) Array.newInstance(eleType, list.size()));
    }

    /**
     * 寻找到给定输入第一个不为 null 的值
     * 
     * @param <T>
     * @param objs
     *            给定输入
     * @return 第一个非 null 值
     */
    @SuppressWarnings("unchecked")
    public static <T> T fallback(T... objs) {
        for (T obj : objs) {
            if (null != obj)
                return obj;
        }
        return null;
    }

    /**
     * 较方便的创建一个数组，比如：
     *
     * <pre>
     * String[] strs = Wlang.array("A", "B", "A"); => ["A","B","A"]
     * </pre>
     *
     * @param eles
     *            可变参数
     * @return 数组对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] array(T... eles) {
        return eles;
    }

    /**
     * 较方便的创建一个没有重复的数组，比如：
     *
     * <pre>
     * String[] strs = Lang.arrayUniq("A","B","A");  => ["A","B"]
     * String[] strs = Lang.arrayUniq();  => null
     * </pre>
     *
     * 返回的顺序会遵循输入的顺序
     *
     * @param eles
     *            可变参数
     * @return 数组对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayUniq(T... eles) {
        if (null == eles || eles.length == 0)
            return null;
        // 记录重复
        HashSet<T> set = new HashSet<T>(eles.length);
        for (T ele : eles) {
            set.add(ele);
        }
        // 循环
        T[] arr = (T[]) Array.newInstance(eles[0].getClass(), set.size());
        int index = 0;
        for (T ele : eles) {
            if (set.remove(ele))
                Array.set(arr, index++, ele);
        }
        return arr;
    }

    /**
     * 将一个对象添加成为一个数组的第一个元素，从而生成一个新的数组
     *
     * @param e
     *            对象
     * @param eles
     *            数组
     * @return 新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayFirst(T e, T[] eles) {
        try {
            if (null == eles || eles.length == 0) {
                T[] arr = (T[]) Array.newInstance(e.getClass(), 1);
                arr[0] = e;
                return arr;
            }
            T[] arr = (T[]) Array.newInstance(eles.getClass().getComponentType(), eles.length + 1);
            arr[0] = e;
            for (int i = 0; i < eles.length; i++) {
                arr[i + 1] = eles[i];
            }
            return arr;
        }
        catch (NegativeArraySizeException e1) {
            throw Wlang.wrapThrow(e1);
        }
    }

    /**
     * 将一个对象添加成为一个数组的最后一个元素，从而生成一个新的数组
     *
     * @param e
     *            对象
     * @param eles
     *            数组
     * @return 新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayLast(T[] eles, T e) {
        try {
            if (null == eles || eles.length == 0) {
                T[] arr = (T[]) Array.newInstance(e.getClass(), 1);
                arr[0] = e;
                return arr;
            }
            T[] arr = (T[]) Array.newInstance(eles.getClass().getComponentType(), eles.length + 1);
            for (int i = 0; i < eles.length; i++) {
                arr[i] = eles[i];
            }
            arr[eles.length] = e;
            return arr;
        }
        catch (NegativeArraySizeException e1) {
            throw Wlang.wrapThrow(e1);
        }
    }

    /**
     * 较方便的创建一个列表
     *
     * <pre>
     * List<String> list = Wlang.list("A", "B", "A"); => ["A","B","A"]
     * </pre>
     *
     * @param eles
     *            可变参数
     * @return 列表对象
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> list(T... eles) {
        List<T> list = new ArrayList<>(eles.length);
        for (T ele : eles) {
            list.add(ele);
        }
        return list;
    }

    /**
     * 较方便的创建一个集合
     *
     * <pre>
     * List<String> list = Wlang.list("A", "B", "A"); => ["A","B","A"]
     * </pre>
     *
     * @param eles
     *            可变参数
     * @return 列表对象
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> set(T... eles) {
        Set<T> set = new HashSet<>();
        for (T ele : eles) {
            set.add(ele);
        }
        return set;
    }

    /**
     * 较方便的创建一个列表
     *
     * <pre>
     * List<String> list = Wlang.list("A", "B", "A"); => ["A","B","A"]
     * </pre>
     *
     * @param eles
     *            可变参数
     * @param offset
     *            数据偏移下标
     * @param len
     *            最多取多少元素
     * @return 列表对象
     */
    public static <T> List<T> list(T[] eles, int offset, int len) {
        offset = Math.max(0, offset);
        len = Math.min(len, eles.length - offset);
        List<T> list = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            T ele = eles[i];
            list.add(ele);
        }
        return list;
    }

    /**
     * map对象浅过滤,返回值是一个新的map
     *
     * @param source
     *            原始的map对象
     * @param prefix
     *            包含什么前缀,并移除前缀
     * @param include
     *            正则表达式 仅包含哪些key(如果有前缀要求,则已经移除了前缀)
     * @param exclude
     *            正则表达式 排除哪些key(如果有前缀要求,则已经移除了前缀)
     * @param keyMap
     *            映射map, 原始key--目标key (如果有前缀要求,则已经移除了前缀)
     * @return 经过过滤的map,与原始map不是同一个对象
     */
    public static Map<String, Object> filter(Map<String, Object> source,
                                             String prefix,
                                             String include,
                                             String exclude,
                                             Map<String, String> keyMap) {
        LinkedHashMap<String, Object> dst = new LinkedHashMap<String, Object>();
        if (source == null || source.isEmpty())
            return dst;

        Pattern includePattern = include == null ? null : Regex.getPattern(include);
        Pattern excludePattern = exclude == null ? null : Regex.getPattern(exclude);

        for (Entry<String, Object> en : source.entrySet()) {
            String key = en.getKey();
            if (prefix != null) {
                if (key.startsWith(prefix))
                    key = key.substring(prefix.length());
                else
                    continue;
            }
            if (includePattern != null && !includePattern.matcher(key).find())
                continue;
            if (excludePattern != null && excludePattern.matcher(key).find())
                continue;
            if (keyMap != null && keyMap.containsKey(key))
                dst.put(keyMap.get(key), en.getValue());
            else
                dst.put(key, en.getValue());
        }
        return dst;
    }

    public static <T> T first(T[] col) {
        if (null == col || col.length == 0) {
            return null;
        }
        return col[0];
    }

    public static <T> T first(Collection<T> col) {
        if (null == col || col.isEmpty()) {
            return null;
        }
        return col.iterator().next();
    }

    /**
     * 如果是数组或集合取得第一个对象。 否则返回自身
     *
     * @param obj
     *            任意对象
     * @return 第一个代表对象
     */
    public static Object firstInAny(Object obj) {
        if (null == obj)
            return obj;

        if (obj instanceof Collection<?>) {
            Iterator<?> it = ((Collection<?>) obj).iterator();
            return it.hasNext() ? it.next() : null;
        }

        if (obj.getClass().isArray())
            return Array.getLength(obj) > 0 ? Array.get(obj, 0) : null;

        return obj;
    }

    /**
     * 获得一个容器（Map/集合/数组）对象包含的元素数量
     * <ul>
     * <li>null : 0
     * <li>数组
     * <li>集合
     * <li>Map
     * <li>一般 Java 对象。 返回 1
     * </ul>
     *
     * @param obj
     * @return 对象长度
     * @since Nutz 1.r.62
     */
    public static int eleSize(Object obj) {
        // 空指针，就是 0
        if (null == obj)
            return 0;
        // 数组
        if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        // 容器
        if (obj instanceof Collection<?>) {
            return ((Collection<?>) obj).size();
        }
        // Map
        if (obj instanceof Map<?, ?>) {
            return ((Map<?, ?>) obj).size();
        }
        // 其他的就是 1 咯
        return 1;
    }

    public static StringBuilder execOutput(String cmd) throws IOException {
        return execOutput(Strings.splitIgnoreBlank(cmd, " "), Encoding.CHARSET_UTF8);
    }

    public static StringBuilder execOutput(String cmd, Charset charset) throws IOException {
        return execOutput(Strings.splitIgnoreBlank(cmd, " "), charset);
    }

    public static StringBuilder execOutput(String cmd[]) throws IOException {
        return execOutput(cmd, Encoding.CHARSET_UTF8);
    }

    public static StringBuilder execOutput(String[] cmd, Charset charset) throws IOException {
        Process p = Runtime.getRuntime().exec(cmd);
        p.getOutputStream().close();
        InputStreamReader r = new InputStreamReader(p.getInputStream(), charset);
        StringBuilder sb = new StringBuilder();
        Streams.readAndClose(r, sb);
        return sb;
    }

    public static void exec(String cmd, StringBuilder out, StringBuilder err) throws IOException {
        exec(Strings.splitIgnoreBlank(cmd, " "), Encoding.CHARSET_UTF8, out, err);
    }

    public static void exec(String[] cmd, StringBuilder out, StringBuilder err) throws IOException {
        exec(cmd, Encoding.CHARSET_UTF8, out, err);
    }

    public static void exec(String[] cmd, Charset charset, StringBuilder out, StringBuilder err)
            throws IOException {
        Process p = Runtime.getRuntime().exec(cmd);
        p.getOutputStream().close();
        InputStreamReader sOut = new InputStreamReader(p.getInputStream(), charset);
        Streams.readAndClose(sOut, out);

        InputStreamReader sErr = new InputStreamReader(p.getErrorStream(), charset);
        Streams.readAndClose(sErr, err);
    }

    /**
     * 将抛出对象包裹成运行时异常，并增加自己的描述
     *
     * @param e
     *            抛出对象
     * @param fmt
     *            格式
     * @param args
     *            参数
     * @return 运行时异常
     */
    public static RuntimeException wrapThrow(Throwable e, String fmt, Object... args) {
        return new RuntimeException(String.format(fmt, args), e);
    }

    /**
     * 用运行时异常包裹抛出对象，如果抛出对象本身就是运行时异常，则直接返回。
     * <p>
     * 如果是 InvocationTargetException，那么将其剥离，只包裹其 TargetException
     *
     * @param e
     *            抛出对象
     * @return 运行时异常
     */
    public static RuntimeException wrapThrow(Throwable e) {
        if (e instanceof RuntimeException)
            return (RuntimeException) e;
        if (e instanceof InvocationTargetException)
            return wrapThrow(((InvocationTargetException) e).getTargetException());
        return new RuntimeException(e);
    }

    /**
     * 一个便利的方法，将当前线程睡眠一段时间
     *
     * @param ms
     *            要睡眠的时间 ms
     */
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            throw Er.wrap(e);
        }
    }

    /**
     * 一个便利的等待方法同步一个对象
     *
     * @param lock
     *            锁对象
     * @param ms
     *            要等待的时间 ms
     */
    public static void wait(Object lock, long ms) {
        if (null != lock)
            synchronized (lock) {
                try {
                    lock.wait(ms);
                }
                catch (InterruptedException e) {
                    throw Er.wrap(e);
                }
            }
    }

    /**
     * 通知对象的同步锁
     *
     * @param lock
     *            锁对象
     */
    public static void notifyAll(Object lock) {
        if (null != lock)
            synchronized (lock) {
                lock.notifyAll();
            }
    }

    /**
     * 获取指定文件的 MD5 值
     *
     * @param f
     *            文件
     * @return 指定文件的 MD5 值
     * @see #digest(String, File)
     */
    public static String md5(File f) {
        return Wsum.md5AsString(f);
    }

    /**
     * 获取指定输入流的 MD5 值
     *
     * @param ins
     *            输入流
     * @return 指定输入流的 MD5 值
     * @see #digest(String, InputStream)
     */
    public static String md5(InputStream ins) {
        return Wsum.md5AsString(ins);
    }

    public static String fixedHexString(byte[] hashBytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
            sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    /**
     * 获取指定字符串的 MD5 值
     *
     * @param cs
     *            字符串
     * @return 指定字符串的 MD5 值
     * @see #digest(String, CharSequence)
     */
    public static String md5(CharSequence cs) {
        return Wsum.md5AsString(cs.toString());
    }

    /**
     * 获取指定文件的 SHA1 值
     *
     * @param f
     *            文件
     * @return 指定文件的 SHA1 值
     * @see #digest(String, File)
     */
    public static String sha1(File f) {
        return Wsum.sha1AsString(f);

    }

    /**
     * 获取指定输入流的 SHA1 值
     *
     * @param ins
     *            输入流
     * @return 指定输入流的 SHA1 值
     * @see #digest(String, InputStream)
     */
    public static String sha1(InputStream ins) {
        return Wsum.sha1AsString(ins);
    }

    /**
     * 获取指定字符串的 SHA1 值
     *
     * @param cs
     *            字符串
     * @return 指定字符串的 SHA1 值
     * @see #digest(String, CharSequence)
     */
    public static String sha1(CharSequence cs) {
        return Wsum.sha1AsString(cs.toString());
    }

    public static String simpleMethodDesc(Method method) {
        return String.format("%s.%s(...)",
                             method.getDeclaringClass().getSimpleName(),
                             method.getName());
    }

    /**
     * 从一个文本输入流读取所有内容，并将该流关闭
     *
     * @param reader
     *            文本输入流
     * @return 输入流所有内容
     */
    public static String readAll(Reader reader) {
        if (!(reader instanceof BufferedReader))
            reader = new BufferedReader(reader);
        try {
            StringBuilder sb = new StringBuilder();

            char[] data = new char[64];
            int len;
            while (true) {
                if ((len = reader.read(data)) == -1)
                    break;
                sb.append(data, 0, len);
            }
            return sb.toString();
        }
        catch (IOException e) {
            throw wrapThrow(e);
        }
        finally {
            Streams.safeClose(reader);
        }
    }

    /**
     * 将一段字符串写入一个文本输出流，并将该流关闭
     *
     * @param writer
     *            文本输出流
     * @param str
     *            字符串
     */
    public static void writeAll(Writer writer, String str) {
        try {
            writer.write(str);
            writer.flush();
        }
        catch (IOException e) {
            throw wrapThrow(e);
        }
        finally {
            Streams.safeClose(writer);
        }
    }

    /**
     * 根据一段文本模拟出一个输入流对象
     *
     * @param cs
     *            文本
     * @return 输出流对象
     */
    public static InputStream ins(CharSequence cs) {
        return new StringInputStream(cs);
    }

    /**
     * 根据一段文本模拟出一个文本输入流对象
     *
     * @param cs
     *            文本
     * @return 文本输出流对象
     */
    public static Reader inr(CharSequence cs) {
        return new StringReader(cs.toString());
    }

    /**
     * 根据一个 StringBuilder 模拟一个文本输出流对象
     *
     * @param sb
     *            StringBuilder 对象
     * @return 文本输出流对象
     */
    public static Writer opw(StringBuilder sb) {
        return new StringWriter(sb);
    }

    /**
     * 根据一个 StringBuilder 模拟一个输出流对象
     *
     * @param sb
     *            StringBuilder 对象
     * @return 输出流对象
     */
    public static StringOutputStream ops(StringBuilder sb) {
        return new StringOutputStream(sb);
    }

    /**
     * 对Thread.sleep(long)的简单封装,不抛出任何异常
     *
     * @param millisecond
     *            休眠时间
     */
    public static void quiteSleep(long millisecond) {
        try {
            if (millisecond > 0)
                Thread.sleep(millisecond);
        }
        catch (Throwable e) {}
    }

    public static Throwable unwrapThrow(Throwable e) {
        if (e == null)
            return null;
        if (e instanceof InvocationTargetException) {
            InvocationTargetException itE = (InvocationTargetException) e;
            if (itE.getTargetException() != null)
                return unwrapThrow(itE.getTargetException());
        }
        if (e instanceof RuntimeException && e.getCause() != null)
            return unwrapThrow(e.getCause());
        return e;
    }

    public static boolean isCauseBy(Throwable e, Class<? extends Throwable> causeType) {
        if (e.getClass() == causeType)
            return true;
        Throwable cause = e.getCause();
        if (null == cause)
            return false;
        return isCauseBy(cause, causeType);
    }

    /**
     * 判断一个对象是否为空。它支持如下对象类型：
     * <ul>
     * <li>null : 一定为空
     * <li>数组
     * <li>集合
     * <li>Map
     * <li>其他对象 : 一定不为空
     * </ul>
     *
     * @param obj
     *            任意对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null)
            return true;
        if (obj.getClass().isArray())
            return Array.getLength(obj) == 0;
        if (obj instanceof Collection<?>)
            return ((Collection<?>) obj).isEmpty();
        if (obj instanceof Map<?, ?>)
            return ((Map<?, ?>) obj).isEmpty();
        return false;
    }

    /**
     * 判断一个对象是否不为空。它支持如下对象类型：
     * <ul>
     * <li>null : 一定为空
     * <li>数组
     * <li>集合
     * <li>Map
     * <li>其他对象 : 一定不为空
     * </ul>
     *
     * @param obj
     *            任意对象
     * @return 是否为空
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 判断一个数组是否是空数组
     *
     * @param ary
     *            数组
     * @return null 或者空数组都为 true 否则为 false
     */
    public static <T> boolean isEmptyArray(T[] ary) {
        return null == ary || ary.length == 0;
    }

    public static boolean isEqual(Object a, Object b) {
        if (null == a && null == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        return a.equals(b);
    }

    /**
     * 判断两个对象是否相等。 这个函数用处是:
     * <ul>
     * <li>可以容忍 null
     * <li>可以容忍不同类型的 Number
     * <li>对数组，集合， Map 会深层比较
     * </ul>
     * 当然，如果你重写的 equals 方法会优先
     *
     * @param a0
     *            比较对象1
     * @param a1
     *            比较对象2
     * @return 是否相等
     */
    public static boolean isEqualDeeply(Object a0, Object a1) {
        if (a0 == a1)
            return true;

        if (a0 == null && a1 == null)
            return true;

        if (a0 == null || a1 == null)
            return false;

        // 简单的判断是否等于
        if (a0.equals(a1))
            return true;

        Mirror<?> mi = Mirror.me(a0);

        // 简单类型，变字符串比较，或者正则表达式
        if (mi.isSimple() || mi.is(Pattern.class)) {
            return a0.toString().equals(a1.toString());
        }

        // 如果类型就不能互相转换，那么一定是错的
        if (!a0.getClass().isAssignableFrom(a1.getClass())
            && !a1.getClass().isAssignableFrom(a0.getClass()))
            return false;

        // Map
        if (a0 instanceof Map && a1 instanceof Map) {
            Map<?, ?> m1 = (Map<?, ?>) a0;
            Map<?, ?> m2 = (Map<?, ?>) a1;
            if (m1.size() != m2.size())
                return false;
            for (Entry<?, ?> e : m1.entrySet()) {
                Object key = e.getKey();
                if (!m2.containsKey(key) || !isEqualDeeply(m1.get(key), m2.get(key)))
                    return false;
            }
            return true;
        }
        // 数组
        else if (a0.getClass().isArray() && a1.getClass().isArray()) {
            int len = Array.getLength(a0);
            if (len != Array.getLength(a1))
                return false;
            for (int i = 0; i < len; i++) {
                if (!isEqualDeeply(Array.get(a0, i), Array.get(a1, i)))
                    return false;
            }
            return true;
        }
        // 集合
        else if (a0 instanceof Collection && a1 instanceof Collection) {
            Collection<?> c0 = (Collection<?>) a0;
            Collection<?> c1 = (Collection<?>) a1;
            if (c0.size() != c1.size())
                return false;

            Iterator<?> it0 = c0.iterator();
            Iterator<?> it1 = c1.iterator();

            while (it0.hasNext()) {
                Object o0 = it0.next();
                Object o1 = it1.next();
                if (!isEqualDeeply(o0, o1))
                    return false;
            }

            return true;
        }

        // 一定不相等
        return false;
    }

    /**
     * 原方法使用线程ClassLoader,各种问题,改回原版.
     */
    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        }
        catch (Throwable e) {
            return Class.forName(className);
        }
    }

    public static Class<?> loadClassQuietly(String className) {
        try {
            return loadClass(className);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 生成一个未实现的运行时异常
     *
     * @return 一个未实现的运行时异常
     */
    public static RuntimeException noImplement() {
        return new RuntimeException("Not implement yet!");
    }

    /**
     * 生成一个不可能的运行时异常
     *
     * @return 一个不可能的运行时异常
     */
    public static RuntimeException impossible() {
        return new RuntimeException("r u kidding me?! It is impossible!");
    }

    /**
     * 根据格式化字符串，生成运行时异常
     *
     * @param format
     *            格式
     * @param args
     *            参数
     * @return 运行时异常
     */
    public static RuntimeException makeThrow(String msg) {
        return new RuntimeException(msg);
    }

    /**
     * 根据格式化字符串，生成运行时异常
     *
     * @param format
     *            格式
     * @param args
     *            参数
     * @return 运行时异常
     */
    public static RuntimeException makeThrow(String format, Object... args) {
        return new RuntimeException(String.format(format, args));
    }

    /**
     * 根据格式化字符串，生成一个指定的异常。
     *
     * @param classOfT
     *            异常类型， 需要有一个字符串为参数的构造函数
     * @param format
     *            格式
     * @param args
     *            参数
     * @return 异常对象
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T makeThrow(Class<T> classOfT,
                                                    String format,
                                                    Object... args) {
        if (classOfT == RuntimeException.class)
            return (T) new RuntimeException(String.format(format, args));
        return Mirror.me(classOfT).born(String.format(format, args));
    }

    public static class JdkTool {
        public static String getVersionLong() {
            Properties sys = System.getProperties();
            return sys.getProperty("java.version");
        }

        public static int getMajorVersion() {
            String ver = getVersionLong();
            if (Strings.isBlank(ver))
                return 6;
            String[] tmp = ver.split("\\.");
            if (tmp.length < 2)
                return 6;
            int t = Integer.parseInt(tmp[0]);
            if (t > 1)
                return t;
            return Integer.parseInt(tmp[1]);
        }

        public static boolean isEarlyAccess() {
            String ver = getVersionLong();
            if (Strings.isBlank(ver))
                return false;
            return ver.contains("-ea");
        }

        /**
         * 获取进程id
         * 
         * @param fallback
         *            如果获取失败,返回什么呢?
         * @return 进程id
         */
        public static String getProcessId(final String fallback) {
            final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            final int index = jvmName.indexOf('@');
            if (index < 1) {
                return fallback;
            }
            try {
                return Long.toString(Long.parseLong(jvmName.substring(0, index)));
            }
            catch (NumberFormatException e) {}
            return fallback;
        }
    }
}
