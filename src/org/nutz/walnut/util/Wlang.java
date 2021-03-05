package org.nutz.walnut.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.nutz.lang.Lang;
import org.nutz.lang.LoopException;
import org.nutz.walnut.util.each.WnBreakException;
import org.nutz.walnut.util.each.WnContinueException;
import org.nutz.walnut.util.each.WnEachIteratee;

public class Wlang {

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
        catch (LoopException e) {
            throw Lang.wrapThrow(e.getCause());
        }
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

}
