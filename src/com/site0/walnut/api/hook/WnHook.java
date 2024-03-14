package com.site0.walnut.api.hook;

import com.site0.walnut.api.io.WnObj;

public interface WnHook {

    boolean match(WnObj o);

    void invoke(WnHookContext hc, WnObj o) throws WnHookBreak;

    String getName();

    String getType();

    String toString();

    String getRunby();

    void setRunby(String runby);
}
