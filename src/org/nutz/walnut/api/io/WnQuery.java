package org.nutz.walnut.api.io;

import java.util.regex.Pattern;

import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;

public class WnQuery extends NutMap {

    private int skip;

    private int limit;

    private NutMap sort;

    private boolean or;

    public WnQuery() {
        this.sort = new NutMap();
    }

    public WnQuery range(String key, Region<?> rg) {
        this.setOrRemove(key, rg);
        return this;
    }

    @Override
    public WnQuery setv(String key, Object value) {
        return (WnQuery) super.setv(key, value);
    }

    public WnQuery setRegex(String key, String regex) {
        return setv(key, Pattern.compile(regex));
    }

    public void reset() {
        clear();
        sort.clear();
        skip(-1);
        limit(-1);
    }

    public boolean isOr() {
        return or;
    }

    public void setOr(boolean or) {
        this.or = or;
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

}
