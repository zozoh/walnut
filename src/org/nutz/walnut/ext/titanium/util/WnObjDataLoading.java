package org.nutz.walnut.ext.titanium.util;

import org.nutz.walnut.api.io.WnObj;

public interface WnObjDataLoading<T> {
    T load(WnObj o);
}
