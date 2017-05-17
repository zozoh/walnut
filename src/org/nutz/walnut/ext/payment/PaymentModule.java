package org.nutz.walnut.ext.payment;

import javax.servlet.http.HttpServletRequest;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.module.AbstractWnModule;

/**
 * 提供基础支付方面的支持
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@At("/pay")
public class PaymentModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    @Inject
    private WnPayment pay;

    /**
     * 创建并提交支付单
     * 
     * @param brief
     *            支付单简要描述
     * @param fee
     *            支付的金额，单位是元
     * @param cur
     *            货币，默认是 RMB
     * @param pay_tp
     *            交易类型
     * @param pay_target
     *            付款的目标商户
     * @param seller
     *            卖家（服务商）域名
     * @param callback
     *            回调脚本名称
     * @param meta
     *            支付单更多元数据
     * @param arg
     *            向第三方平台发送支付单时更多参数，比如付款码
     * @return 支付单详情
     */
    @Filters(@By(type = WnCheckSession.class))
    @At("/ajax/do")
    public NutBean ajax_do(@Param("br") String brief,
                           @Param("fe") int fee,
                           @Param("cu") String cur,
                           @Param("pt") String pay_tp,
                           @Param("ta") String pay_target,
                           @Param("s") String seller,
                           @Param("cb") String callback,
                           @Param("m") String meta,
                           @Param("a") String arg) {
        // 准备创建支付单的信息
        WnPayInfo wpi = new WnPayInfo();
        wpi.buyer_nm = Wn.WC().checkMe(); // 当前操作的用户是 buyer
        wpi.seller_nm = seller;
        wpi.brief = brief;
        wpi.fee = fee;
        wpi.cur = cur;
        wpi.callbackName = callback;
        if (!Strings.isBlank(meta)) {
            wpi.meta = Lang.map(meta);
        }

        // 创建支付单
        WnPayObj po = pay.create(wpi);

        // 发送支付单
        pay.send(po, pay_tp, pay_target, arg);

        // 输出返回值
        return po.toBean();
    }

    /**
     * 获取支付单详情
     * 
     * @param poId
     *            支付单 ID
     * @param check
     *            是否尝试去第三方平台检查支付单状况
     * @return 支付单详情
     */
    public NutBean ajax_get(@Param("id") String poId, @Param("c") boolean check) {
        WnPayObj po = pay.get(poId, false);

        if (check) {
            pay.check(po);
        }

        // 输出返回值
        return po.toBean();
    }

    /**
     * 异步回调：微信
     * 
     * @param req
     *            请求对象
     */
    public void noti_by_weixin(HttpServletRequest req) {

    }

    /**
     * 异步回调：支付宝
     * 
     * @param req
     *            请求对象
     */
    @At("/noti/alipay")
    @Ok("raw")
    public String noti_by_alipay(@Param("..") NutMap params) {
        if (log.isInfoEnabled())
            log.infof("@-noti-by-alipay: %s", Json.toJson(params, JsonFormat.nice()));
        // 根据参数找到支付单
        String poId = params.getString("out_trade_no");
        if (Strings.isBlank(poId))
            throw Er.create("e.pay.out_trade_no.noexist");
        WnPayObj po = pay.get(poId, false);

        // 调用结束
        pay.complete(po, params);

        // 告诉支付宝，表再调了
        return "success";
    }

    /**
     * 同步回调：支付宝
     * 
     * @param req
     *            请求对象
     */
    public void retn_by_alipay(HttpServletRequest req) {

    }

}
