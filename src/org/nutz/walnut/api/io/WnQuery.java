package org.nutz.walnut.api.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wlang;

public class WnQuery {

    private int skip;

    private int limit;

    private NutMap sort;

    private List<NutMap> list;

    public WnQuery() {
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

    public WnQuery unset(String... keys) {
        NutMap ele = first();
        for (String key : keys) {
            ele.remove(key);
        }
        return this;
    }

    public WnQuery setvIfNoBlank(String key, String value) {
        if (!Strings.isBlank(value)) {
            this.setv(key, value);
        }
        return this;
    }

    public WnQuery setLongRegion(String key, String tm) {
        if (!Strings.isBlank(tm)) {
            // 不存在
            if ("0".equals(tm)) {
                this.setv(key, Lang.map("$exists", false));
            }
            // 区间
            else {
                this.setv(key, tm);
            }
        }
        return this;
    }

    public WnQuery setvToList(String key, Object value) {
        if (null == list) {
            list = new ArrayList<>(5);
        }
        if (list.isEmpty()) {
            list.add(Wlang.map(key, value));
        } else {
            for (NutMap ele : list) {
                ele.setv(key, value);
            }
        }
        return this;
    }

    public WnQuery setAll(Map<String, Object> map) {
        NutMap ele = first();
        ele.setAll(map);
        return this;
    }

    public WnQuery unsetAll(String... keys) {
        if (null != list) {
            for (NutMap ele : list)
                for (String key : keys) {
                    ele.remove(key);
                }
        }
        return this;
    }

    public WnQuery setAllToList(Map<String, Object> map) {
        if (list.isEmpty()) {
            list.add(new NutMap());
        }
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

    public boolean hasSkip() {
        return skip > 0;
    }

    public int skip() {
        return skip;
    }

    public WnQuery skip(int skip) {
        this.skip = skip;
        return this;
    }

    public boolean hasLimit() {
        return limit > 0;
    }

    public int limit() {
        return limit;
    }

    public WnQuery limit(int limit) {
        this.limit = limit;
        return this;
    }

    public NutMap sort() {
        if (null == sort) {
            synchronized (this) {
                if (null == sort) {
                    sort = new NutMap();
                }
            }
        }
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

    public WnQuery exists(String nm, boolean isExist) {
        return setv(nm, new NutMap("$exists", isExist));
    }

    public String toString() {
        return String.format("+%d : %d @ %s: %s",
                             skip,
                             limit,
                             Json.toJson(sort, JsonFormat.compact().setQuoteName(false)),
                             Json.toJson(list,
                                         JsonFormat.forLook()
                                                   .setQuoteName(false)
                                                   .setIgnoreNull(false)));
    }
}
