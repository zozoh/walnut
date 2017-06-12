package org.nutz.walnut.ext.backup;

import org.nutz.log.Log;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.ZParams;

public interface WnImpExp {

    void exp(String root, ZParams params, Log log, WnSession se);
    
    void imp(String dump, String root, ZParams params, Log log);
}
