package com.site0.walnut.core;

import com.site0.walnut.api.io.WnObj;

public interface WnIoActionCallback {

    WnObj on_before(WnObj o);

    WnObj on_after(WnObj o);

}
