package org.nutz.walnut.api.io;

import org.nutz.walnut.util.ZParams;

public interface WnImpExp {

    void exp(String root, ZParams params);
    
    void imp(String root, ZParams params);
}
