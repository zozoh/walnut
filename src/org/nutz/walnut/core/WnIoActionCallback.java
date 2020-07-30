package org.nutz.walnut.core;

import org.nutz.walnut.api.io.WnObj;

public interface WnIoActionCallback {

    void on_before(WnObj o);

    void on_after(WnObj o);

}
