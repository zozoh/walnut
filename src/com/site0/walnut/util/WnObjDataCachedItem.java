package com.site0.walnut.util;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

public class WnObjDataCachedItem<T> {

    private T data;

    private String objFinger;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isMatchFinger(WnIo io, WnObj o) {
        String finger = Wn.getEtag(o, io);
        return objFinger.equals(finger);
    }

    public void setObj(WnIo io, WnObj o) {
        this.objFinger = Wn.getEtag(o, io);
    }

}
