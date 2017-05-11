package org.nutz.walnut.ext.payment.hdl;

import java.util.List;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.payment.WnPayInfo;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.payment.WnPayment;
import org.nutz.walnut.ext.payment.WnPays;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;

@JvmHdlParamArgs(value = "lcqnbish", regex = "^(pager|json)$")
public class pay_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 准备支付单信息
        WnPayInfo wpi = WnPays.genPayInfo(hc.params.get("bu"), hc.params.get("se"));
        wpi.brief = hc.params.val(0);

        // 准备得到查询条件
        WnQuery q = new WnQuery();

        // 更多匹配条件
        if (hc.params.has("match")) {
            String json = hc.params.get("match");
            q.add(Lang.map(json));
        }

        // 简介
        q.setvIfNoBlank(WnPayObj.KEY_BRIEF, Wn.toQueryRegex(wpi.brief));

        // 买家
        if (wpi.hasBuyterType())
            q.setv(WnPayObj.KEY_BUYER_TP, wpi.buyer_tp);
        q.setvIfNoBlank(WnPayObj.KEY_BUYER_ID, wpi.buyer_id);
        q.setvIfNoBlank(WnPayObj.KEY_BUYER_NM, wpi.buyer_nm);

        // 卖家
        q.setvIfNoBlank(WnPayObj.KEY_SELLER_ID, wpi.seller_id);
        q.setvIfNoBlank(WnPayObj.KEY_SELLER_NM, wpi.seller_nm);

        // 价格
        q.setvIfNoBlank(WnPayObj.KEY_FEE, hc.params.get("fee"));
        q.setvIfNoBlank(WnPayObj.KEY_CUR, hc.params.get("cur"));

        // 时间
        q.setLongRegion(WnPayObj.KEY_SEND_AT, hc.params.get("s_at"));
        q.setLongRegion(WnPayObj.KEY_CLOSE_AT, hc.params.get("c_at"));

        // 支付单状状态
        q.setvIfNoBlank(WnPayObj.KEY_ST, hc.params.get("st"));

        // 分页
        WnPager wp = new WnPager(hc.params);

        NutMap sort = null;
        if (hc.params.has("sort")) {
            sort = Lang.map(hc.params.check("sort"));
        }
        q.sort(sort);

        // 查询结果
        List<WnPayObj> list = pay.query(q);

        // 输出
        Cmds.output_objs(sys, hc.params, wp, list, false);
    }

}
