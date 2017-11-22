package org.nutz.walnut.ext.voucher.hdl;

import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 创建或修改一个促销活动
 * @author wendal
 *
 */
public class voucher_create_promotion implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String voucher_name = hc.params.check("name"); // 活动简称
        String voucher_title = hc.params.check("title"); // 活动标题
        int totalNum = hc.params.checkInt("totalNum"); //优惠卷总量
        long startTime = Times.ams(hc.params.check("startTime")); // 起始时间,为时间戳
        long endTime = Times.ams(hc.params.check("endTime")); // 结束时间,也是时间戳
        String[] scope = hc.params.has("scope") ? hc.params.get("scope").split(",") : new String[0]; // 指定范围
        int condition = hc.params.getInt("condition"); // 最低启用金额
        double discount = hc.params.checkInt("discount"); // 满减金额或折扣率
        String myName = sys.me.name();
        sys.nosecurity(()->{
            // 如果活动已经存在,是不是禁止更新属性才对??
            WnObj wobj = sys.io.createIfNoExists(null, "/var/voucher/"+myName + "/" + voucher_name, WnRace.DIR);
            NutMap metas = new NutMap();
            metas.setv("voucher_title", voucher_title);
            metas.setv("voucher_totalNum", totalNum > 1000 || totalNum < 1 ? 1000 : totalNum);
            metas.setv("voucher_startTime", startTime);
            metas.setv("voucher_endTime", endTime);
            metas.setv("voucher_scope", scope);
            metas.setv("voucher_condition", condition);
            metas.setv("voucher_discount", discount);
            sys.io.appendMeta(wobj, metas);
            sys.out.writeJson(sys.io.get(wobj.id()));
        });
    }

}
