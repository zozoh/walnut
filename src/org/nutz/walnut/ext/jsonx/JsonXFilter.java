package org.nutz.walnut.ext.jsonx;

import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public interface JsonXFilter {
    
    ZParams parseParams(String[] args);

    void process(WnSystem sys, JsonXContext ctx, ZParams params);

}
