package org.nutz.walnut.ext.jsonx;

import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public abstract class JsonXFilter {

    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, null);
    }

    abstract protected void process(WnSystem sys, JsonXContext ctx, ZParams params);

}
