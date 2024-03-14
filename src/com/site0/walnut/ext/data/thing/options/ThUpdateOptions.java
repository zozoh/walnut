package com.site0.walnut.ext.data.thing.options;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.WnExecutable;

public class ThUpdateOptions {

    public static ThUpdateOptions create(NutMap meta, WnExecutable executor, Object match) {
        ThUpdateOptions re = new ThUpdateOptions();
        re.meta = meta;
        re.executor = executor;
        re.match = match;
        return re;
    }

    public NutMap meta;
    public WnExecutable executor;
    public Object match;
    public boolean withoutHook;
}
