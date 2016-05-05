package org.nutz.walnut.ext.noti.impl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.noti.WnNotiHandler;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;
import org.nutz.weixin.spi.WxResp;

public class WnWeixinNotiHandler implements WnNotiHandler {

    @Override
    public NutMap add(WnSystem sys, ZParams params) {
        NutMap meta = new NutMap();

        meta.put("noti_target", params.check("to"));
        meta.put("noti_wx_pnb", params.check("pnb"));
        meta.put("noti_wx_tmpl_id", params.check("tmpl"));
        meta.put("noti_weixin_tmpl_content",
                 Lang.map(Cmds.checkParamOrPipe(sys, params, "content")));

        if (params.has("url"))
            meta.put("noti_wx_tmpl_url", params.get(""));

        return meta;
    }

    @Override
    public String send(WnSystem sys, WnObj oN) {
        String openid = oN.getString("noti_target");
        String pnb = oN.getString("noti_wx_pnb");
        String tid = oN.getString("noti_wx_tmpl_id");
        String content = Json.toJson(oN.get("noti_weixin_tmpl_content"), JsonFormat.compact());
        String url = oN.getString("noti_wx_tmpl_url");

        String cmdText = String.format("weixin %s tmpl -to '%s' -tid '%s' -content",
                                       pnb,
                                       openid,
                                       tid);
        if (!Strings.isBlank(url)) {
            cmdText += " -url '" + url + "'";
        }
        String reStr = sys.exec2(cmdText, content);

        WxResp re = Json.fromJson(WxResp.class, reStr);
        if (!re.ok())
            return re.errcode() + " : " + re.errmsg();
        return null;
    }

}
