package com.site0.walnut.ext.data.site.render;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnQuery;

public class SiteRenderArchive {
    /**
     * 归档集合的名称
     */
    private String name;

    /**
     * 从哪里加载归档，也同时会根据这个路径自动生成 rph
     */
    private String base;

    /**
     * 输出的目标路径， 默认就是 rph， 渲染器会保证路径以.html结束
     */
    private String dist;

    /**
     * 一个当前归档元数据为上下文的解释集合，作为渲染 wnml 的上下文
     */
    private NutMap vars;

    /**
     * 符合条件的对象会被渲染
     */
    private NutMap filter;

    /**
     * 排序
     */
    private NutMap sort;

    /**
     * 符合条件的对象会被递归
     */
    private Object recur;

    /**
     * 为了防止意外，要规定一个单层查询最大数据限制，默认2000,
     */
    private int limit;

    public boolean isSameName(String name) {
        return null != this.name && this.name.equals(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String path) {
        this.base = path;
    }

    public String getDist() {
        return dist;
    }

    public void setDist(String dist) {
        this.dist = dist;
    }

    public NutMap getVars() {
        return vars;
    }

    public void setVars(NutMap vars) {
        this.vars = vars;
    }

    public boolean hasFilter() {
        return null != filter && !filter.isEmpty();
    }

    public NutMap getFilter() {
        return filter;
    }

    public void setFilter(NutMap filter) {
        this.filter = filter;
    }

    public boolean hasSort() {
        return null != sort && !sort.isEmpty();
    }

    public NutMap getSort() {
        return sort;
    }

    public void setSort(NutMap sort) {
        this.sort = sort;
    }

    public Object getRecur() {
        return recur;
    }

    public void setRecur(Object recur) {
        this.recur = recur;
    }

    public void setQueryLimit(WnQuery q) {
        if (this.limit > 0) {
            q.limit(this.limit);
        } else {
            q.limit(2000);
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
