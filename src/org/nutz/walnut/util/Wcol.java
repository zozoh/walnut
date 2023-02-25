package org.nutz.walnut.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 容器帮助类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class Wcol {

    /**
     * @param <E>
     *            容器元素泛型
     * @param list
     *            列表
     * @return 列表中第一个元素
     */
    public static <E> E first(List<E> list) {
        if (null == list || list.isEmpty()) {
            return null;
        }
        if (list instanceof LinkedList<?>) {
            LinkedList<E> ll = (LinkedList<E>) list;
            return ll.getFirst();
        }
        return list.get(0);
    }

    /**
     * @param <E>
     *            容器元素泛型
     * @param list
     *            列表
     * @return 列表中最后一个元素
     */
    public static <E> E last(List<E> list) {
        if (null == list || list.isEmpty()) {
            return null;
        }
        if (list instanceof LinkedList<?>) {
            LinkedList<E> ll = (LinkedList<E>) list;
            return ll.getLast();
        }
        return list.get(list.size() - 1);
    }

    /**
     * 将输入容器的内容去重，并填充到输出容器中
     * 
     * @param <E>
     *            容器元素泛型
     * @param col
     *            输入容器
     * @param output
     *            输出容器
     */
    public static <E> void uniq(Collection<E> col, Collection<E> output) {
        if (col.isEmpty())
            return;

        HashSet<E> set = new HashSet<>();
        for (E ele : col) {
            set.add(ele);
        }

        output.addAll(set);
    }

    /**
     * 将容器内对象合并为一个字符串
     * 
     * @param col
     *            容器
     * @param sep
     *            分隔符
     * @return 合并后的字符串
     */
    public static String join(Collection<?> col, String sep) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = col.iterator();
        if (it == null || !it.hasNext())
            return sb.toString();
        sb.append(it.next());
        while (it.hasNext())
            sb.append(sep).append(it.next());
        return sb.toString();
    }

}
