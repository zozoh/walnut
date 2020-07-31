package org.nutz.walnut.core;

import org.nutz.walnut.api.io.WnObj;

public interface WnIoActionCallback {

    WnObj on_before(WnObj o);

    WnObj on_after(WnObj o);

}
