package org.nutz.walnut.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.each.WnBreakException;
import org.nutz.walnut.util.each.WnContinueException;
import org.nutz.walnut.util.each.WnEachIteratee;

public class Wlang {
    
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
}
