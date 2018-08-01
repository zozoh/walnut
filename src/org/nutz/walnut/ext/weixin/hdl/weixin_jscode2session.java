package org.nutz.walnut.ext.weixin.hdl;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.weixin.WxConf;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.Webs.Err;

public class weixin_jscode2session implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);
        WxConf conf = wxApi.getConfig();
        String code = hc.params.val_check(0);
        String url = String.format("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code", conf.appID, conf.appsecret, code);
        Response resp = Http.get(url, 15000);
        if (resp.isOK()) {
            sys.out.print(resp.getContent());
        }
        else {
            throw Err.create("e.cmd_weixin_jscode2session.status_" + resp.getStatus());
        }
    }

}
