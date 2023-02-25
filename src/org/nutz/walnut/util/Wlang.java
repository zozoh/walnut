package org.nutz.walnut.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.nutz.castor.Castors;
import org.nutz.castor.FailToCastObjectException;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.reflect.ReflectTool;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.each.WnBreakException;
import org.nutz.walnut.util.each.WnContinueException;
import org.nutz.walnut.util.each.WnEachIteratee;

public class Wlang {

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
        if (toType.isArray())
            return (T) Lang.collection2array(src.values(), toType.getComponentType());
        // List
        if (List.class == toType) {
            return (T) Lang.collection2list(src.values());
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
                        vv = Lang.collection2array(c, ft.getComponentType());
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
                                throw Lang.wrapThrow(e);
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
        String json = Json.toJson(input);
        return Json.fromJson(NutMap.class, json);
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
     * String[] strs = Lang.array("A", "B", "A"); => ["A","B","A"]
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
            throw Lang.wrapThrow(e1);
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
            throw Lang.wrapThrow(e1);
        }
    }

    /**
     * 较方便的创建一个列表
     *
     * <pre>
     * List<String> list = Lang.list("A", "B", "A"); => ["A","B","A"]
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
     * 较方便的创建一个列表
     *
     * <pre>
     * List<String> list = Lang.list("A", "B", "A"); => ["A","B","A"]
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

}
