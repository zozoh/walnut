package org.nutz.walnut.ext.weixin;

import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.weixin.bean.WxInMsg;

public abstract class WxUtil {

    public static void saveToObj(WxInMsg im, WnObj o) {
        Map<String, Object> imMap = Lang.obj2map(im);
        for (Map.Entry<String, Object> en : imMap.entrySet()) {
            o.setOrRemove("weixin_" + en.getKey(), en.getValue());
        }
    }

    public static WxInMsg getFromObj(WnObj o) {
        NutMap map = new NutMap();
        for (Map.Entry<String, Object> en : o.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            if ("weixin_context".equals(key))
                continue;

            if (key.startsWith("weixin_")) {
                map.put(key.substring("weixin_".length()), val);
            }
        }
        return Lang.map2Object(map, WxInMsg.class);
    }

}
