package org.nutz.walnut.ext.iot.mt90;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.biz.wooz.WoozMap;

public class Mt90Map {
    
    public static Map<String, WoozMap> maps = new HashMap<>();
    
    public static WoozMap get(WnIo io, String path) {
        WnObj tmp = io.check(null, path);
        WoozMap map = maps.get(tmp.sha1());
        if (map == null) {
            map = io.readJson(tmp, WoozMap.class);
            maps.put(tmp.sha1(), map);
        }
        return map;
    }
}
