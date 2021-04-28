package org.nutz.walnut.ext.geo.lbs.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.geo.lbs.bean.LbsFreight;
import org.nutz.walnut.ext.geo.lbs.bean.LbsFreightRule;
import org.nutz.walnut.ext.geo.lbs.bean.LbsFreightSheet;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.ajax.Ajax;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class lbs_freight implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String fromAddr = hc.params.val_check(0);
        String toAddr = hc.params.val_check(1);
        float weight = hc.params.getFloat("weight", 0.0f);
        String country = hc.params.getString("country", "CN");
        String phConf = hc.params.get("conf", "~/.domain/freight_sheet.json");

        // 解析价格表
        WnObj oConf = Wn.checkObj(sys, phConf);
        String json = sys.io.readText(oConf);
        LbsFreightSheet sheet = Json.fromJson(LbsFreightSheet.class, json);

        // 寻找规则
        LbsFreightRule rule = sheet.findRule(country, fromAddr, toAddr);

        // 计算
        LbsFreight price = null;
        if (null != rule) {
            price = sheet.calculatePrice(rule, weight);
        }

        // 输出
        Object re = price;
        if (hc.params.is("ajax")) {
            if (null == rule) {
                re = Ajax.fail().setErrCode("e.cmd.lbs.freight.norule");
            }
            // 没有重量
            else if (weight <= 0) {
                re = Ajax.fail().setErrCode("e.cmd.lbs.freight.noweight");
            }
            // 计算成功
            else {
                re = Ajax.ok().setData(re);
            }
        }
        json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
