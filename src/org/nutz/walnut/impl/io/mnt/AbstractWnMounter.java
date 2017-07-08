package org.nutz.walnut.impl.io.mnt;

import java.util.regex.Pattern;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.io.WnMounter;

public abstract class AbstractWnMounter implements WnMounter {

    public void create(WnObj p, WnObj o) {}
    public void remove(WnObj obj) {}
    public void set(String id, NutMap map){};
    
    protected Pattern namePatten(String name) {
        Pattern ptn = null;

        if (null != name) {
            // 正则
            if (name.startsWith("^")) {
                ptn = Pattern.compile(name);
            }
            // 通配符
            else if (name.contains("*")) {
                ptn = Pattern.compile("^" + name.replace("*", ".*"));
            }
            else
                ptn = Pattern.compile(name);
        }
        return ptn;
    }
}
