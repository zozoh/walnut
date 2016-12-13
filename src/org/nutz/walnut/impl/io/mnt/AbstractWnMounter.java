package org.nutz.walnut.impl.io.mnt;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.WnMounter;

public abstract class AbstractWnMounter implements WnMounter {

    public void create(WnObj p, WnObj o) {}
    public void remove(WnObj obj) {}
    public void set(String id, NutMap map){};
}
