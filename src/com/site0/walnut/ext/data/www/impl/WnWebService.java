package com.site0.walnut.ext.data.www.impl;

import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.auth.WnAuthSetup;
import com.site0.walnut.api.auth.WnCaptchaService;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.www.bean.WnWebSite;
import com.site0.walnut.ext.net.weixin.WnIoWeixinApi;
import com.site0.walnut.impl.auth.DomainAuthEventGenerator;
import com.site0.walnut.impl.auth.WnAuthServiceImpl;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class WnWebService {

    private WnWebSite site;

    private WnAuthSetup setup;

    private WnOrderService order;

    private WnAuthServiceImpl auth;

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

        // 设置事件相关
        __setup_events();

        // 如果定义了订单，那么创建订单服务（以便处理产品/优惠券/订单）
        if (site.hasOrderHome()) {
            order = new WnOrderService(io, site);
        }
    }

    private void __setup_events() {
        if (!site.hasHistory())
            return;

        // 增加一个事件发生器
        DomainAuthEventGenerator eg = new DomainAuthEventGenerator(site.getDomainGroup(),
                                                                   site.getDomainHomePath());
        auth.setEventGenerator(eg);

        // 准备监听器实例
        WnAuthHistoryEventListener lis = new WnAuthHistoryEventListener(site);

        // 增加历史记录监听
        for (String eventName : site.getHistoryEventNames()) {
            auth.addEventListener(eventName, lis);
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
