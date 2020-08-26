package org.nutz.walnut.core.bean;

import org.nutz.walnut.api.io.WnIoIndexer;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.core.WnIoMapping;

public class WnObjMapping extends WnObjId {

    public WnObjMapping(String str) {
        super(str);
    }

    /**
     * 如果对象有子映射，则为映射根对象。否则为全局根对象
     */
    private WnObj home;

    /**
     * 全局根映射
     */
    private WnIoMapping globalMapping;

    /**
     * 子映射
     */
    private WnIoMapping mapping;

    public WnObj getHome() {
        return home;
    }

    public void setHome(WnObj oHome) {
        this.home = oHome;
    }

    public WnIoMapping getGlobalMapping() {
        return globalMapping;
    }

    public void setGlobalMapping(WnIoMapping globalMapping) {
        this.globalMapping = globalMapping;
    }

    public WnIoMapping getMapping() {
        return mapping;
    }

    public void setMapping(WnIoMapping mapping) {
        this.mapping = mapping;
    }

    public WnIoMapping getSelfMapping() {
        if (this.isInSubMapping()) {
            return this.mapping;
        }
        return this.globalMapping;
    }

    public WnIoIndexer getSelfIndexer() {
        return this.getSelfMapping().getIndexer();
    }

    public WnIoMapping getSubMapping() {
        if (null != this.mapping) {
            return this.mapping;
        }
        return this.globalMapping;
    }

    public WnIoIndexer getSubIndexer() {
        return this.getSubMapping().getIndexer();
    }

}
