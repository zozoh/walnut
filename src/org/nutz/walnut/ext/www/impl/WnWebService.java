package org.nutz.walnut.ext.www.impl;

import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSetup;
import org.nutz.walnut.api.auth.WnCaptchaService;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.www.bean.WnWebSite;
import org.nutz.walnut.impl.auth.WnAuthServiceImpl;
import org.nutz.walnut.impl.box.WnSystem;

public class WnWebService {

    private WnWebSite site;

    private WnAuthSetup setup;

    private WnOrderService order;

    private WnAuthService auth;

    public WnWebService(WnSystem sys, WnObj oWWW) {
        String siteId = oWWW.id();

        String homePath = sys.getHome().getRegularPath();
        site = new WnWebSite(sys.io, homePath, siteId, oWWW);

        setup = new WnWebAuthSetup(sys.io, site);
        auth = new WnAuthServiceImpl(sys.io, setup);
        order = new WnOrderService(sys.io, site);
    }

    /**
     * @return 站点信息
     */
    public WnWebSite getSite() {
        return this.site;
    }

    /**
     * @return 微信服务接口
     */
    public WnIoWeixinApi getWeixinApi() {
        return setup.getWeixinApi();
    }

    /**
     * @return 验证码服务接口
     */
    public WnCaptchaService getCaptchaApi() {
        return setup.getCaptchaService();
    }

    /**
     * @return 订单服务接口
     */
    public WnOrderService getOrderApi() {
        return order;
    }

    /**
     * @return 账户及会话服务接口
     */
    public WnAuthService getAuthApi() {
        return auth;
    }

}
