package com.site0.walnut.ext.net.payment.hdl;

import java.util.List;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.ext.net.payment.WnPayInfo;
import com.site0.walnut.ext.net.payment.WnPayObj;
import com.site0.walnut.ext.net.payment.WnPayment;
import com.site0.walnut.ext.net.payment.WnPays;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnPager;

@JvmHdlParamArgs(value = "lcqnbish", regex = "^(pager|json)$")
public class pay_query implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 准备支付单信息
        String bu = hc.params.check("bu");
        String sl = hc.params.get("se");
        WnPayInfo wpi = new WnPayInfo();
        wpi.fillBuyer(bu);
        wpi.seller_nm = sl;
        wpi.brief = hc.params.val(0);

        // 准备得到查询条件
        WnQuery q = new WnQuery();

        // 更多匹配条件
        if (hc.params.has("match")) {
            String json = hc.params.get("match");
            q.add(Wlang.map(json));
        }

        // 简介
        q.setvIfNoBlank(WnPays.KEY_BRIEF, Wn.toQueryRegex(wpi.brief));

        // 买家
        if (wpi.hasBuyterType())
            q.setv(WnPays.KEY_BUYER_TP, wpi.buyer_tp);
        q.setvIfNoBlank(WnPays.KEY_BUYER_ID, wpi.buyer_id);
        q.setvIfNoBlank(WnPays.KEY_BUYER_NM, wpi.buyer_nm);

        // 卖家
        q.setvIfNoBlank(WnPays.KEY_SELLER_ID, wpi.seller_id);
        q.setvIfNoBlank(WnPays.KEY_SELLER_NM, wpi.seller_nm);

        // 价格
        q.setvIfNoBlank(WnPays.KEY_FEE, hc.params.get("fee"));
        q.setvIfNoBlank(WnPays.KEY_CUR, hc.params.get("cur"));

        // 时间
        q.setLongRegion(WnPays.KEY_SEND_AT, hc.params.get("s_at"));
        q.setLongRegion(WnPays.KEY_CLOSE_AT, hc.params.get("c_at"));

        // 支付单状状态
        q.setvIfNoBlank(WnPays.KEY_ST, hc.params.get("st"));

        // 分页
        WnPager wp = new WnPager(hc.params);

        NutMap sort = null;
        if (hc.params.has("sort")) {
            sort = Wlang.map(hc.params.check("sort"));
        }
        q.sort(sort);

        // 查询结果
        wp.setupQuery(sys, q);
        List<WnPayObj> list = pay.query(q);

        // 输出
        Cmds.output_objs(sys, hc.params, wp, list, false);
    }

}
