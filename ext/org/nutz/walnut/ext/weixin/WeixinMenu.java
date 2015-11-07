package org.nutz.walnut.ext.weixin;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.weixin.spi.WxApi2;
import org.nutz.weixin.spi.WxResp;

public class WeixinMenu {

    public void handle(WnSystem sys, ZParams params) {
        String str = params.check("menu");

        // 创建微信 API
        WxApi2 wxApi = WxUtil.newWxApi(sys, params);

        WxResp resp;
        // 删除菜单
        if (":delete".equals(str)) {
            resp = wxApi.menu_delete();
        }
        // 获取菜单
        else if (":get".equals(str)) {
            resp = wxApi.menu_get();
        }
        // 创建菜单
        else {
            String json;
            // 从管道读取
            if (sys.pipeId > 0) {
                json = sys.in.readAll();
            }
            // 从对象读取
            else {
                WnObj o = Wn.checkObj(sys, str);
                json = sys.io.readText(o);
            }

            NutMap map = Json.fromJson(NutMap.class, json);
            resp = wxApi.menu_create(map);
        }

        // 输出
        JsonFormat df = JsonFormat.nice().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sys.out.println(Json.toJson(resp, df));
    }
}
