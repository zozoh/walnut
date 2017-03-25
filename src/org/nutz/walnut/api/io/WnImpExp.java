package org.nutz.walnut.api.io;

import java.io.IOException;

import org.nutz.log.Log;
import org.nutz.walnut.util.ZParams;

public interface WnImpExp {

    void exp(String root, ZParams params, Log log);
    
    void imp(String dump, String root, ZParams params, Log log) throws IOException;
}
