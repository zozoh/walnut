package org.nutz.walnut.api.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;

public class WnQuery {

    private int skip;

    private int limit;

    private NutMap sort;

    private List<NutMap> list;

    public WnQuery() {
        this.sort = new NutMap();
        this.list = new ArrayList<NutMap>(5);
    }

    public WnQuery add(NutMap map) {
        list.add(map);
        return this;
    }

    public WnQuery addAll(List<NutMap> maps) {
        list.addAll(maps);
        return this;
    }

    public List<NutMap> getList() {
        return list;
    }

    public boolean isEmptyMatch() {
        for (NutMap ele : list) {
            if (ele.size() > 0)
                return false;
        }
        return true;
    }

    public NutMap first() {
        NutMap ele;
        if (list.isEmpty()) {
            ele = new NutMap();
            list.add(ele);
        } else {
            ele = list.get(0);
        }
        return ele;
    }

    public NutMap item(int index) {
        if (index > 0 && index < list.size())
            return list.get(index);
        return null;
    }

    public WnQuery setv(String key, Object value) {
        NutMap ele = first();
        ele.setv(key, value);
        return this;
    }

    public WnQuery setvToList(String key, Object value) {
        for (NutMap ele : list) {
            ele.setv(key, value);
        }
        return this;
    }

    public WnQuery setAll(Map<String, Object> map) {
        NutMap ele = first();
        ele.setAll(map);
        return this;
    }

    public WnQuery setAllToList(Map<String, Object> map) {
        for (NutMap ele : list) {
            ele.setAll(map);
        }
        return this;
    }

    public void reset() {
        list.clear();
        sort.clear();
        skip(-1);
        limit(-1);
    }

    public int skip() {
        return skip;
    }

    public WnQuery skip(int skip) {
        this.skip = skip;
        return this;
    }

    public int limit() {
        return limit;
    }

    public WnQuery limit(int limit) {
        this.limit = limit;
        return this;
    }

    public NutMap sort() {
        return sort;
    }

    public WnQuery sort(NutMap sort) {
        this.sort = sort;
        return this;
    }

    /**
     * 设置某个字段的排序
     * 
     * @param nm
     *            排序字符串名
     * @param order
     *            排序的值 ASC(1), DESC(-1);
     * 
     * @return 自身以便链式赋值
     */
    public WnQuery sortBy(String nm, int order) {
        sort().put(nm, order);
        return this;
    }

    public WnQuery asc(String nm) {
        return sortBy(nm, 1);
    }

    public WnQuery desc(String nm) {
        return sortBy(nm, -1);
    }

    public String toString() {
        return String.format("+%d : %d @ %s: %s",
                             skip,
                             limit,
                             Json.toJson(sort, JsonFormat.compact().setQuoteName(false)),
                             Json.toJson(list, JsonFormat.forLook().setQuoteName(false)));
    }
}
