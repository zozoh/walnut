package org.nutz.walnut.ext.payment.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPayInfo;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.payment.WnPayment;
import org.nutz.walnut.ext.payment.WnPays;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

public class pay_create implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 准备创建支付单的信息
        WnPayInfo wpi = WnPays.genPayInfo(hc.params.check("bu"), hc.params.get("se"));

        // 填充支付单的 buyer 的名称和ID，必须配对
        if (wpi.isWnUsr()) {
            this.__check_buyer_as_wn(sys, wpi);
        }
        // 用域用户的方式检测
        else if (wpi.isDUsr()) {
            this.__check_buyer_as_domain(sys, wpi);
        }
        // 不可能
        else {
            throw Lang.impossible();
        }

        // 填充简介
        wpi.brief = hc.params.get("br");

        // 填充费用
        WnPays.fillFee(wpi, hc.params.check("fee"));

        // 填充优惠券对象
        String couponId = hc.params.get("co");
        if (!Strings.isBlank(couponId) && !"true".equals(couponId)) {
            wpi.coupon = sys.io.checkById(couponId);
            wpi.couponScope = hc.params.get("scope");
        }

        // 回调
        if (hc.params.has("callback")) {
            wpi.callbackName = hc.params.get("callback");
        }

        // 更多元数据
        String json = Cmds.getParamOrPipe(sys, hc.params, "meta", false);
        if (!Strings.isBlank(json)) {
            wpi.meta = Lang.map(json);
        }

        // 创建支付单
        WnPayObj po = pay.create(wpi);

        // 金额免费的话，直接完成支付单
        if (po.getFee() <= 0) {
            // 发送支付单
            pay.send(po, "free", null);

            // 完成支付单
            WnPay3xRe re = pay.complete(po, null);

            // 看看有没有必要调用回调
            WnPays.try_callback(sys, po);

            // 输出完成的结果
            sys.out.println(Json.toJson(re,
                                        JsonFormat.nice()
                                                  .setQuoteName(true)
                                                  .setIgnoreNull(false)
                                                  .setLocked("^(changedKeys)$")));
        }
        // 指定了类型，继续发送支付单
        else if (hc.params.has("pt")) {
            // 得到支付类型
            String payType = hc.params.get("pt");

            // 得到支付目标s
            String payTarget = hc.params.check("ta");

            // 得到参数
            String[] args = hc.params.vals;

            // 发送支付单
            WnPay3xRe re = pay.send(po, payType, payTarget, args);

            // 输出发送结果
            sys.out.println(Json.toJson(re,
                                        JsonFormat.nice()
                                                  .setQuoteName(true)
                                                  .setIgnoreNull(false)
                                                  .setLocked("^(changedKeys)$")));
        }
        // 输出
        else {
            sys.out.println(Json.toJson(po));
        }
    }

    private void __check_buyer_as_domain(WnSystem sys, WnPayInfo wpi) {
        boolean noBuId = Strings.isBlank(wpi.buyer_id);
        boolean noBuNm = Strings.isBlank(wpi.buyer_nm);

        // 没有设置买家
        if (noBuId) {
            throw Er.create("e.pay.craete.nobuyer");
        }

        // 补足
        if (noBuNm) {
            // 没有设置买家
            if (noBuId && noBuNm) {
                throw Er.create("e.pay.craete.nobuyer");
            }
            WnObj u = sys.io.checkById(wpi.buyer_id);
            wpi.buyer_nm = u.getString("th_nm", u.name());
        }

    }

    private void __check_buyer_as_wn(WnSystem sys, WnPayInfo wpi) {
        boolean noBuId = Strings.isBlank(wpi.buyer_id);
        boolean noBuNm = Strings.isBlank(wpi.buyer_nm);

        // 补足
        if (noBuId || noBuNm) {
            // 没有设置买家
            if (noBuId && noBuNm) {
                throw Er.create("e.pay.craete.nobuyer");
            }
            // 补足 ID
            if (noBuId) {
                WnUsr u = sys.usrService.check(wpi.buyer_nm);
                wpi.buyer_id = u.id();
                wpi.buyer_nm = u.name();
            }
            // 补足名称
            else {
                WnUsr u = sys.usrService.check("id:" + wpi.buyer_id);
                wpi.buyer_nm = u.name();
            }
        }
        // 如果两个都有，则比较一下是否匹配
        else {
            WnUsr u = sys.usrService.check(wpi.buyer_nm);
            if (!u.isSameId(wpi.buyer_id)) {
                throw Er.create("e.pay.create.noMatchBuyer", wpi.buyer_id + " not " + wpi.buyer_nm);
            }
        }
    }

}
