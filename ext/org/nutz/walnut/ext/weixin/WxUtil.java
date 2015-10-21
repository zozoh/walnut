package org.nutz.walnut.ext.weixin;

import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.weixin.bean.WxInMsg;

public abstract class WxUtil {

    public static void saveToObj(WxInMsg im, WnObj o) {
        Map<String, Object> imMap = Lang.obj2map(im);
        for (Map.Entry<String, Object> en : imMap.entrySet()) {
            String key = Strings.upperFirst(en.getKey());
            o.setOrRemove("weixin_" + key, en.getValue());
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
                String objKey = key.substring("weixin_".length());
                map.put(Strings.lowerFirst(objKey), val);
            }
        }
        return Lang.map2Object(map, WxInMsg.class);
    }

    public static WnIoWeixinApi newWxApi(final WnSystem sys, ZParams params) {
        WnObj oConf = null;
        String pnb = params.get("pnb");
        if (Strings.isBlank(pnb)) {
            // 获取当前目录
            String pwd = sys.se.envs().getString("PWD");
            String path = Wn.normalizePath(pwd, sys);
            WnObj oCurrent = sys.io.check(null, path);
            oConf = sys.io.fetch(oCurrent, "wxconf");

            // 如果当前文件夹下有 wxconf，则表示为主目录
            WnObj o = oCurrent;
            while (null == oConf && o.hasParent()) {
                o = o.parent();
                oConf = sys.io.fetch(oCurrent, "wxconf");
            }

            if (null == oConf)
                throw Er.create("e.weixin.lack.pnb");

        }
        // 创建微信 API
        else {
            oConf = sys.io.check(null, Wn.normalizeFullPath("~/.weixin/" + pnb + "/wxconf", sys));
        }
        WnIoWeixinApi wxApi = new WnIoWeixinApi(sys.io, oConf);
        return wxApi;
    }

}
