package org.nutz.walnut.ext.media.mediax.bean;

import org.nutz.lang.util.NutMap;

/**
 * 记录搜刮接口某一个搜刮的结果
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class MxReCrawl extends NutMap {

    public MxReCrawl index(int index) {
        this.put("__I", index);
        return this;
    }

    public int index() {
        return this.getInt("__I");
    }

}
