package org.nutz.walnut.impl.box.weixin;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.weixin.impl.WxApi2Impl;

public class WnIoWeixinApi extends WxApi2Impl {

    public WnIoWeixinApi(WnIo io, WnObj home) {
        // 设置 token 存取方式
        this.setAccessTokenStore(new WnIoWxAccessTokenStore(io, home));
        this.setJsapiTicketStore(new WnIoWxJsapiTicketStore(io, home));

        // 从 "~/.weixin/wxconf" 对象中读取必要的信息
        WnObj oConf = io.check(home, "wxconf");
        WxConf conf = io.readJson(oConf, WxConf.class);

        this.appid = conf.appID;
        this.appsecret = conf.appsecret;
        this.token = conf.token;

    }

}
