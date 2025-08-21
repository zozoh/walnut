package com.site0.walnut.ext.xo.util;

import com.site0.walnut.util.Wlang;

public class WnSimpleClientGetter<T> implements XoClientGetter<T> {

    private XoClientWrapper<T> client;

    public WnSimpleClientGetter(XoClientWrapper<T> client) {
        this.client = client;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (null == other)
            return false;

        if (!(other instanceof WnSimpleClientGetter<?>)) {
            return false;
        }

        WnSimpleClientGetter<?> get = (WnSimpleClientGetter<?>) other;
        return Wlang.isEqual(this.client, get.client);
    }

    @Override
    public XoClientWrapper<T> get() {
        return client;
    }

}
