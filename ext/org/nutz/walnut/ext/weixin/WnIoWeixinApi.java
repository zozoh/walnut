package org.nutz.walnut.ext.weixin;

import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.weixin.impl.WxApi2Impl;

public class WnIoWeixinApi extends WxApi2Impl {

    private WnObj oConf;

    private WnObj oHome;

    private WxConf config;

    public WnIoWeixinApi(WnIo io, WnObj oConf) {
        if (!oConf.isFILE()) {
            throw Er.create("e.wxapi.noconf", oConf);
        }

        // 读取配置文件
        // 从 "~/.weixin/wxconf" 对象中读取必要的信息
        this.config = io.readJson(oConf, WxConf.class);

        // 配置文件所在的目录为主目录
        this.oHome = oConf.parent();
        this.oConf = oConf;

        // 设置 token 存取方式
        this.setAccessTokenStore(new WnIoWxAccessTokenStore(io, oHome));
        this.setJsapiTicketStore(new WnIoWxJsapiTicketStore(io, oHome));

        this.appid = config.appID;
        this.appsecret = config.appsecret;
        this.token = config.token;

    }

    public WxConf getConfig() {
        return config;
    }

    public WnObj getHomeObj() {
        return oHome;
    }

    public WnObj getConfObj() {
        return oConf;
    }

}
