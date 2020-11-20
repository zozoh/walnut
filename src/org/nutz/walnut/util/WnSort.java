package org.nutz.walnut.util;

import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;

public class WnSort {

    public static WnSort[] makeList(String json) {
        NutMap map = Lang.map(json);
        return makeList(map);
    }

    public static WnSort[] makeList(Map<String, Object> map) {
        // 防守
        if (null == map)
            return null;

        WnSort[] sorts = new WnSort[map.size()];
        int i = 0;
        for (Map.Entry<String, Object> en : map.entrySet()) {
            sorts[i++] = new WnSort(en);
        }
        return sorts;
    }

    /**
     * @param sorts
     *            比较列表
     * @param o1
     *            对象1
     * @param o2
     *            对象2
     * @param nullAs
     *            如果对象1为null，对象2不为null，返回什么，-1 还是 1 还是 0。 <br>
     *            也就是说，null 与 非null 的比较，值应该是什么<br>
     *            推荐传入 <code>-1</code>
     * @return 返回比较值，
     *         <ul>
     *         <li><code>-1</code> : <code>o1 < o2</code>
     *         <li><code>0</code> : <code>o1 == o2</code>
     *         <li><code>1</code> : <code>o1 > o2</code>
     *         </ul>
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static int compare(WnSort[] sorts,
                              Map<String, Object> o1,
                              Map<String, Object> o2,
                              int nullAs) {
        for (WnSort sort : sorts) {
            Object v1 = o1.get(sort.name);
            Object v2 = o2.get(sort.name);
            if (null == v1) {
                // 都是空，继续比
                if (null == v2) {
                    continue;
                }
                // 相等，继续比
                if (0 == nullAs) {
                    continue;
                }
                // 否则有的那个比较大
                return nullAs * sort.order;
            }
            if (null == v2) {
                if (0 == nullAs) {
                    continue;
                }
                return nullAs * -1 * sort.order;
            }
            // 如果两个对象有一个是不可比较的，那么都是 0
            if (v1 instanceof Comparable) {
                if (v2 instanceof Comparable) {
                    try {
                        Comparable c1 = (Comparable) v1;
                        Comparable c2 = (Comparable) v2;
                        int re = c1.compareTo(c2);
                        if (0 == re) {
                            continue;
                        }
                        return re * sort.order;
                    }
                    // 比较失败，全当 0
                    catch (Exception e) {
                        continue;
                    }
                }
            }

        }
        return 0;
    }

    /**
     * @see #compare(WnSort[], Map, Map, int)
     */
    public static int compare(WnSort[] sorts, Map<String, Object> o1, Map<String, Object> o2) {
        return compare(sorts, o1, o2, -1);
    }

    /**
     * 排序的键名
     */
    public String name;

    /**
     * 排序方式
     * <ul>
     * <li><code>1</code> : ASC : 从小到大
     * <li><code>-1</code> : DESC : 从大到小
     * </ul>
     */
    public int order;

    public WnSort() {}

    public WnSort(String name, int order) {
        this.name = name;
        this.order = order;
    }

    public WnSort(Map.Entry<String, Object> en) {
        this.name = en.getKey();
        this.order = Castors.me().castTo(en.getValue(), Integer.class);
    }

}
