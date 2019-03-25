package org.nutz.walnut.ext.titanium;

import org.nutz.walnut.api.io.WnObj;

public class WnObjCachedItem<T> {

    private T data;

    private String objFinger;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isMatchFinger(WnObj o) {
        return objFinger.equals(o.sha1());
    }

    public void setObj(WnObj o) {
        this.objFinger = o.sha1();
    }

}
