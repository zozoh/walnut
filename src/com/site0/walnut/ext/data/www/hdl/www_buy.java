package com.site0.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.www.cmd_www;
import com.site0.walnut.ext.data.www.bean.WnCoupon;
import com.site0.walnut.ext.data.www.bean.WnOrder;
import com.site0.walnut.ext.data.www.impl.WnOrderService;
import com.site0.walnut.ext.data.www.impl.WnWebService;
import com.site0.walnut.ext.sys.mq.WnMqApi;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.web.WnConfig;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_buy implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
        WnObj oWWW = cmd_www.checkSite(sys, hc);
        String ticket = hc.params.get("ticket");

        // -------------------------------
        // 支付单走消息队列
        String mqTopic = hc.params.getString("mq");
        WnMqApi mqApi = null;
        if (!Strings.isBlank(mqTopic)) {
            WnConfig conf = hc.ioc.get(WnConfig.class, "conf");
            if (conf.getBoolean("mq-enabled", false)) {
                String beanName = conf.get("mq-api-name", "messageQueueApi");
                mqApi = hc.ioc.get(WnMqApi.class, beanName);
            }
        }

        // -------------------------------
        // 准备服务类
        WnWebService webs = new WnWebService(sys, oWWW);
        WnOrderService orderApi = webs.getOrderApi();

        // -------------------------------
        // 检查会话
        WnAuthSession se = webs.getAuthApi().checkSession(ticket);
        WnAccount bu = se.getMe();
        WnObj oAccontHome = webs.getSite().getAccountHome();

        // -------------------------------
        // 读取订单数据
        String input = sys.in.readAll();

        // -------------------------------
        // 准备订单数据
        WnOrder or = Json.fromJson(WnOrder.class, input);
        or.setBuyerId(bu.getId());
        or.setAccounts(oAccontHome.id());

        // -------------------------------
        // 执行订单的创建
        String priceRuleKey = hc.params.getString("prices", "prices");
        String skuKey = hc.params.getString("sku_by", "sku");
        if ("nil".equals(skuKey)) {
            skuKey = null;
        }
        or = orderApi.createOrder(or, priceRuleKey, bu, skuKey);

        // -------------------------------
        // 标识优惠券被使用
        if (or.hasCoupons()) {
            NutMap cpnMeta = new NutMap();
            cpnMeta.put("cpn_used", System.currentTimeMillis());
            cpnMeta.put("cpn_orid", or.getId());
            cpnMeta.put("cpn_stat", 1);
            for (WnCoupon cpn : or.getCoupons()) {
                sys.io.setBy(cpn.getId(), cpnMeta, false);
            }
        }

        // -------------------------------
        // 准备支付单
        if (or.hasPayType()) {
            NutMap upick = hc.params.getAs("upick", NutMap.class);
            // 走消息队列
            if (null != mqApi) {
                String cmd = "www pay 'id:%s' %s -ticket '%s' -cqn -ajax";
                String body = String.format(cmd, oWWW.id(), or.getId(), ticket);
                if (null != upick && !upick.isEmpty()) {
                    body += " -upick '" + Json.toJson(upick) + "'";
                }
                // 推入消息队列
                StringBuilder sbOut = new StringBuilder();
                StringBuilder sbErr = new StringBuilder();
                sys.exec("mq send -t " + mqTopic, sbOut, sbErr, body);
                if (sbErr.length() > 0) {
                    String sfmt = "www_buy fail to mq send: %s\n  - body: %s";
                    String errMsg = String.format(sfmt, sbErr, body);
                    throw Er.create("e.cmd.www_buy.FailMqSend", errMsg);
                }
            }
            // 直接创建
            else {
                cmd_www.prepareToPayOrder(sys, webs, or, bu, upick);
            }
        }
        // -------------------------------
        // 输出结果
        cmd_www.outputOrder(sys, hc, or);
    }

}
