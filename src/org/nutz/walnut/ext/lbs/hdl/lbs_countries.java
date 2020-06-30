package org.nutz.walnut.ext.lbs.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.ext.lbs.cmd_lbs;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.ajax.Ajax;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class lbs_countries implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String lang = hc.params.getString("lang", "zh_cn");
        String map = hc.params.getString("map");

        Object re;

        // 映射为对象
        if ("obj".equals(map)) {
            re = cmd_lbs.getCountryMap(lang, true);
        }
        // 映射为名称
        else if ("name".equals(map)) {
            re = cmd_lbs.getCountryMap(lang, false);
        }
        // 直接就是列表
        else {
            re = cmd_lbs.getCountries(lang);
        }

        if (hc.params.is("ajax")) {
            re = Ajax.ok().setData(re);
        }

        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
