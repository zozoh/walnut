package com.site0.walnut.util;

import com.site0.walnut.api.io.WnObj;

public interface WnObjDataLoading<T> {
    T load(WnObj o);
}
