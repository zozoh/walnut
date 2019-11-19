package org.nutz.walnut.ext.www.hdl;

import java.util.LinkedList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.bean.WnOrderStatus;
import org.nutz.walnut.ext.www.bean.WnWebSession;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.ajax.Ajax;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_pay implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
        String site = hc.params.val_check(0);
        String orId = hc.params.val_check(1);
        String ticket = hc.params.check("ticket");

        // -------------------------------
        // 准备服务类
        WnObj oWWW = Wn.checkObj(sys, site);
        WnObj oDomain = Wn.checkObj(sys, "~/.domain");
        WnWebService webs = new WnWebService(sys, oWWW, oDomain);

        // -------------------------------
        // 得到订单
        WnOrder or = webs.checkOrder(orId);
        String payType = or.getPayType();

        // -------------------------------
        // 检查用户登录票据
        WnWebSession se = webs.checkSession(ticket);
        WnObj bu = se.getMe();

        // -------------------------------
        // 准备元数据
        NutMap meta = new NutMap();
        meta.put("or_id", or.getId());

        // -------------------------------
        // 检查回调
        WnObj oCallback = Wn.getObj(sys, "~/.domain/payment/after");

        // -------------------------------
        // 准备命令
        List<String> cmds = new LinkedList<>();
        cmds.add("pay create");
        if (!Strings.isBlank(payType)) {
            cmds.add("-pt '" + payType + "'");
            cmds.add("-ta strato");
        }
        cmds.add("-br '" + or.getTitle() + "'");
        cmds.add("-bu " + bu.parentId() + ":" + bu.id());
        cmds.add("-fee " + (int) (or.getFee() * 100f));
        if (null != oCallback) {
            cmds.add("-callback id:" + oCallback.id());
        }
        cmds.add("-meta");

        String cmdText = Strings.join(" ", cmds);

        // -------------------------------
        // 执行
        String input = Json.toJson(meta, JsonFormat.compact());
        String re = sys.exec2(cmdText, input);
        WnPay3xRe payRe = Json.fromJson(WnPay3xRe.class, re);
        String payId = payRe.getPayObjId();

        // 更新订单的支付单关联
        or.setPayId(payId);
        or.setStatus(WnOrderStatus.WT);
        or.setWaitAt(System.currentTimeMillis());
        NutMap orMeta = or.toMeta("^(pay_id|wt_at)$", null);
        webs.updateOrder(or.getId(), orMeta);

        // -------------------------------
        // 解析命令结果并输出
        or.setPayReturn(payRe);
        Object reo = or;
        if (hc.params.is("ajax")) {
            reo = Ajax.ok().setData(or);
        }
        hc.jfmt.setLocked("^(c|m|g|d0|d1|md|tp|mime|ph|pid|data|sha1|len)$");
        String json = Json.toJson(reo, hc.jfmt);
        sys.out.println(json);
    }

}
