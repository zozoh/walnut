package org.nutz.walnut.ext.titanium.util;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;

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
        String finger = Wn.getEtag(o);
        return objFinger.equals(finger);
    }

    public void setObj(WnObj o) {
        this.objFinger = Wn.getEtag(o);
    }

}
