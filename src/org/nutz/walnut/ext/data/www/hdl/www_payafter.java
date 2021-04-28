package org.nutz.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.www.cmd_www;
import org.nutz.walnut.ext.data.www.bean.WnOrder;
import org.nutz.walnut.ext.data.www.bean.WnOrderStatus;
import org.nutz.walnut.ext.data.www.bean.WnProduct;
import org.nutz.walnut.ext.data.www.bean.WnWebSite;
import org.nutz.walnut.ext.data.www.impl.WnWebService;
import org.nutz.walnut.ext.net.payment.WnPayObj;
import org.nutz.walnut.ext.net.payment.WnPayment;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_payafter implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 订单 ID
        String orId = hc.params.val_check(0);

        // -------------------------------
        // 得到订单
        WnObj oOrder = sys.io.get(orId);
        if (null == oOrder) {
            AjaxReturn reo = Ajax.fail();
            reo.setErrCode("e.www.pay.noOrder");
            reo.setData(orId);
            do_ouput(sys, hc, reo);
            return;
        }
        // 格式化订单对象
        WnOrder or = new WnOrder();
        or.updateBy(oOrder);
        // 如果有支付单的话，则检查支付单的状况
        if (!or.hasPayId()) {
            AjaxReturn reo = Ajax.fail();
            reo.setErrCode("e.www.pay.noPayId");
            reo.setData(orId);
            do_ouput(sys, hc, reo);
            return;
        }

        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);
        WnPayObj po = pay.get(or.getPayId(), false);

        // -------------------------------
        // 准备要更新的元数据
        NutMap meta = null;

        // 支付成功
        if (null != po && po.isStatusOk()) {
            // 保持幂等，没有应用过回调才会应用一下
            if (!po.isApplied()) {
                // 【简单订单】的话，直接设置为完成
                if (or.isTypeQ()) {
                    meta = __do_Q_order(or);
                }
                // 默认当作【标准订单】，那么就仅仅标志一下支付成功
                else {
                    meta = __do_A_order(sys, hc, or, po);
                }

                // 订单支付后的
                if (or.hasProducts()) {
                    // 从购物车里删除商品
                    if (hc.params.has("basket")) {
                        __remove_products_from_basket(sys, hc, or, po);
                    }
                    // 记录历史记录
                    if (hc.params.has("site")) {
                        __add_order_history(sys, hc, or, meta);
                    }
                }
            }
        }
        // 支付失败
        else if (null != po && po.isStatusFail()) {
            or.setStatus(WnOrderStatus.FA);
            or.setExpireTime(0);
            or.setFailAt(po.getLong("close_at"));
            meta = or.toMeta("^(or_st|expi|fa_at)$", null);
        }
        // 还未支付
        else {
            AjaxReturn reo = Ajax.fail();
            reo.setErrCode("e.www.pay.waiting");
            reo.setData(orId);
            do_ouput(sys, hc, reo);
            return;
        }

        // -------------------------------
        // 更新订单
        if (null != meta) {
            sys.io.appendMeta(oOrder, meta);
            or.updateBy(oOrder);
        }

        // -------------------------------
        // 解析命令结果并输出
        do_ouput(sys, hc, Ajax.ok().setData(or));
    }

    private void __add_order_history(WnSystem sys, JvmHdlContext hc, WnOrder or, NutMap meta) {
        String sitePath = hc.params.getString("site");
        WnObj oWWW = cmd_www.checkSite(sys, sitePath);

        // 准备服务类
        WnWebService webs = new WnWebService(sys, oWWW);
        WnWebSite site = webs.getSite();

        // 得到用户
        WnAccount u = webs.getAuthApi().checkAccountById(or.getBuyerId());

        // 准备订单元数据
        NutMap orBean = or.toMeta();
        orBean.putAll(meta);

        // 准备订单历史记录上下文
        NutMap bean = new NutMap();
        bean.put("@domain", site.getDomainGroup());
        bean.put("@home", site.getDomainHomePath());
        bean.put("@me", u.toBean());
        bean.put("@order", orBean);

        // 订单类型
        String ortp = Strings.sBlank(or.getType(), "A");

        // 插入订单历史记录:
        __add_order_history(site, bean, "order:pay");
        __add_order_history(site, bean, "order:" + ortp + ":pay");

        // 处理商品历史记录
        for (WnProduct prod : or.getProducts()) {
            bean.put("@product", prod.toBean());
            __add_order_history(site, bean, "order:products");
            __add_order_history(site, bean, "order:" + ortp + ":products");
        }
    }

    private void __add_order_history(WnWebSite site, NutMap bean, String eventName) {
        bean.put("@name", eventName);
        site.addHistoryRecord(bean, eventName);
    }

    private NutMap __do_A_order(WnSystem sys, JvmHdlContext hc, WnOrder or, WnPayObj po) {
        or.setStatus(WnOrderStatus.OK);
        or.setExpireTime(0);
        or.setOkAt(po.getLong("close_at"));
        return or.toMeta("^(or_st|expi|ok_at)$", null);
    }

    private void __remove_products_from_basket(WnSystem sys,
                                               JvmHdlContext hc,
                                               WnOrder or,
                                               WnPayObj po) {
        String buyConf = hc.params.getString("buy");
        String cmd = "buy it -quiet ";
        if (!Strings.isBlank(buyConf)) {
            cmd += "-conf '" + buyConf + "' ";
        }
        // 逐个商品执行删除
        for (WnProduct wp : or.getProducts()) {
            String cmdText = cmd + po.getBuyerId() + " " + wp.getId() + " " + wp.getAmount() * -1;
            sys.exec2(cmdText);
        }
    }

    private NutMap __do_Q_order(WnOrder or) {
        long now = Wn.now();
        or.setStatus(WnOrderStatus.DN);
        or.setExpireTime(0);
        or.setOkAt(now);
        or.setShipAt(now);
        or.setDoneAt(now);
        return or.toMeta("^(or_st|expi|(ok|sp|dn)_at)$", null);
    }

    private void do_ouput(WnSystem sys, JvmHdlContext hc, Object reo) {
        hc.jfmt.setLocked("^(c|m|g|d0|d1|md|tp|mime|ph|pid|data|sha1|len)$");
        String json = Json.toJson(reo, hc.jfmt);
        sys.out.println(json);
    }

}
