package org.nutz.walnut.ext.www.impl;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.www.bean.WnCoupon;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.bean.WnOrderStatus;
import org.nutz.walnut.ext.www.bean.WnProduct;

public class WnOrderService {

    private WnThingService orders;
    private WnThingService products;
    private WnThingService coupons;

    public WnOrderService(WnIo io, WnObj oOrderHome, WnObj oProductHome, WnObj oCouponHome) {
        this.orders = new WnThingService(io, oOrderHome);
        this.products = new WnThingService(io, oProductHome);
        this.coupons = new WnThingService(io, oCouponHome);
    }

    public WnOrder createOrder(WnOrder or) {
        // 防守检查：没产品不行啊
        if (!or.hasProducts()) {
            throw Er.create("e.www.order.nil.products");
        }
        // 防守检查：一些关键信息
        if (Strings.isBlank(or.getAccounts())) {
            throw Er.create("e.www.order.nil.accounts");
        }
        if (Strings.isBlank(or.getSeller())) {
            throw Er.create("e.www.order.nil.seller");
        }
        if (Strings.isBlank(or.getBuyerId())) {
            throw Er.create("e.www.order.nil.buyer_id");
        }
        if (Strings.isBlank(or.getPayType())) {
            throw Er.create("e.www.order.nil.pay_tp");
        }

        // 依次检查产品列表
        for (WnProduct pro : or.getProducts()) {
            WnObj oPro = products.checkThing(pro.getId(), false);
            pro.updateBy(oPro);
        }

        // 检查优惠券列表
        if (or.hasCoupons()) {
            for (WnCoupon cpn : or.getCoupons()) {
                WnObj oCpn = coupons.checkThing(cpn.getId(), false);
                cpn.updateBy(oCpn);
            }
        }

        // 计算订单总价
        float totalPrice = 0;
        for (WnProduct pro : or.getProducts()) {
            totalPrice += pro.getPrice();
        }
        or.setPrice(totalPrice);

        // 应用优惠券
        float fee = totalPrice;
        if (or.hasCoupons()) {
            for (WnCoupon cpn : or.getCoupons()) {
                // 不符合条件，无视
                if (!cpn.canUse(totalPrice)) {
                    continue;
                }
                // 叠加应用这张优惠券
                fee = cpn.apply(fee);
            }
        }
        or.setFee(fee);

        // 准备设置其他字段
        or.setStatus(WnOrderStatus.NW);

        // 准备订单元数据
        NutMap meta = or.toMeta();
        meta.pickAndRemove("id", "ct", "lm");

        // 创建订单对象
        WnObj oOr = orders.createThing(meta);
        or.updateBy(oOr);

        // 返回创建后的订单
        return or;
    }

    public WnOrder checkOrder(String id) {
        WnOrder or = this.getOrder(id);
        if (null == or) {
            throw Er.create("e.www.order.noexits");
        }
        return or;
    }

    public WnOrder getOrder(String id) {
        WnObj oOr = orders.getThing(id, false);
        if (null != oOr) {
            WnOrder or = new WnOrder();
            or.updateBy(oOr);
            return or;
        }
        return null;
    }

}
