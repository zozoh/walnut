package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.payment.WnPayment;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.bean.WnOrderStatus;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_payafter implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
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
        if (null!=po && po.isStatusOk()) {
            or.setStatus(WnOrderStatus.OK);
            or.setOkAt(po.getLong("close_at"));
            meta = or.toMeta("^(st|ok_at)$", null);
        }
        // 支付失败
        else if (null!=po && po.isStatusFail()) {
            or.setStatus(WnOrderStatus.FA);
            or.setFailAt(po.getLong("close_at"));
            meta = or.toMeta("^(st|fa_at)$", null);
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

    private void do_ouput(WnSystem sys, JvmHdlContext hc, Object reo) {
        hc.jfmt.setLocked("^(c|m|g|d0|d1|md|tp|mime|ph|pid|data|sha1|len)$");
        String json = Json.toJson(reo, hc.jfmt);
        sys.out.println(json);
    }

}
