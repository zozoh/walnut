package org.nutz.walnut.ext.www.impl;

import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSetup;
import org.nutz.walnut.api.auth.WnCaptchaService;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.www.bean.WnWebSite;
import org.nutz.walnut.impl.auth.WnAuthServiceImpl;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class WnWebService {

    private WnWebSite site;

    private WnAuthSetup setup;

    private WnOrderService order;

    private WnAuthService auth;

    public WnWebService(WnSystem sys, WnObj oWWW) {
        String homePath = sys.getHome().getRegularPath();
        this._init(sys.io, homePath, oWWW);
    }

    public WnWebService(WnIo io, String homePath, WnObj oWWW) {
        this._init(io, homePath, oWWW);
    }

    public WnWebService(WnIo io, WnObj oWWW) {
        // 默认域路径为 /$d0/$d1
        String homePath = Wn.getObjHomePath(oWWW);

        this._init(io, homePath, oWWW);
    }

    private void _init(WnIo io, String homePath, WnObj oWWW) {
        String siteId = oWWW.id();

        site = new WnWebSite(io, homePath, siteId, oWWW);

        setup = new WnWebAuthSetup(io, site);
        auth = new WnAuthServiceImpl(io, setup);
        // 如果定义了订单，那么创建订单服务（以便处理产品/优惠券/订单）
        if (site.hasOrderHome()) {
            order = new WnOrderService(io, site);
        }
    }

    /**
     * @return 站点信息
     */
    public WnWebSite getSite() {
        return this.site;
    }

    /**
     * @param codeType
     *            公号类型(mp | gh | open)
     * @return 微信服务接口
     */
    public WnIoWeixinApi getWeixinApi(String codeType) {
        return setup.getWeixinApi(codeType);
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
