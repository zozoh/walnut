package org.nutz.walnut.impl.box.weixin;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.weixin.spi.WxApi2;
import org.nutz.weixin.spi.WxResp;

@IocBean
public class WeixinMenu {

    public void handle(WnSystem sys, String str, String pnb) {
        // 得到微信的配置目录
        WnObj wxHome = sys.io.check(null, Wn.normalizeFullPath("~/.weixin/" + pnb, sys));

        // 创建微信 API
        WxApi2 wxApi = new WnIoWeixinApi(sys.io, wxHome);

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
            WnObj o = Wn.checkObj(sys, str);
            NutMap map = sys.io.readJson(o, NutMap.class);
            resp = wxApi.menu_create(map);
        }

        // 输出
        JsonFormat df = JsonFormat.nice().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sys.out.writeLine(Json.toJson(resp, df));
    }
}
