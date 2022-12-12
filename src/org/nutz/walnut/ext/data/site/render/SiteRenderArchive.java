package org.nutz.walnut.ext.data.site.render;

import org.nutz.lang.util.NutMap;

public class SiteRenderArchive {

    /**
     * 会根据这个路径自动生成 rph
     */
    private String homePath;

    /**
     * 符合条件的对象会被渲染
     */
    private NutMap filter;

    /**
     * 符合条件的对象会被递归
     */
    private NutMap recur;

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String htmlPth) {
        this.homePath = htmlPth;
    }

    public NutMap getFilter() {
        return filter;
    }

    public void setFilter(NutMap filter) {
        this.filter = filter;
    }

    public NutMap getRecur() {
        return recur;
    }

    public void setRecur(NutMap recur) {
        this.recur = recur;
    }

}
