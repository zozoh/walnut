package org.nutz.walnut.util;

import org.nutz.walnut.api.io.WnObj;

public interface WnObjDataLoading<T> {
    T load(WnObj o);
}
