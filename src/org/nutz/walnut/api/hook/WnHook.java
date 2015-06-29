package org.nutz.walnut.api.hook;

import org.nutz.walnut.api.io.WnObj;

public interface WnHook {
    
    boolean match(WnObj o);

    void invoke(WnHookContext hc, WnObj o) throws WnHookBreak;

}
