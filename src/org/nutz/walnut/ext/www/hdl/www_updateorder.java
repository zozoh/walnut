package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Nums;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.bean.WnOrderStatus;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_updateorder implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 订单 ID
        String orId = hc.params.val_check(0);
        String m_freight_key = hc.params.getString("m_freight_key", "freight_m");
        String m_total_key = hc.params.getString("m_total_key", "total_m");

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

        // 得到修改过的运费
        float m_freight = oOrder.getFloat(m_freight_key, -1);

        // 得到修改过的商品总价
        float m_total = oOrder.getFloat(m_total_key, -1);

        // 有一个需要修改，也得修改
        if (m_freight >= 0 || m_total >= 0) {
            // 修改总价
            float total = or.getTotal();
            if (m_total >= 0) {
                total = m_total;
            }

            // 修改运费
            float freight = or.getFreight();
            if (m_freight >= 0) {
                freight = m_freight;
            }

            // 计算支付费用
            float fee = Nums.precision(total + freight - or.getDiscount(), 2);
            if (or.getFee() != fee) {
                or.setFee(fee);

                // 更新入订单
                NutMap meta = or.toMeta("^(fee)$", null);
                sys.io.appendMeta(oOrder, meta);
            }
        }

        // 如果已经填写运单号，则修修改发货状态
        if (or.getStatus() == WnOrderStatus.OK && or.hasWaybilCompany() && or.hasWaybilNumber()) {
            or.setShipAt(Wn.now());
            or.setStatus(WnOrderStatus.SP);
            NutMap meta = or.toMeta("^(or_st|sp_at)$", null);
            sys.io.appendMeta(oOrder, meta);
        }

        // 准备输出
        Object reo = or;
        if (hc.params.is("ajax")) {
            reo = Ajax.ok().setData(or);
        }
        do_ouput(sys, hc, reo);
    }

    private void do_ouput(WnSystem sys, JvmHdlContext hc, Object reo) {
        hc.jfmt.setLocked("^(c|m|g|d0|d1|md|tp|mime|ph|pid|data|sha1|len)$");
        String json = Json.toJson(reo, hc.jfmt);
        sys.out.println(json);
    }
}