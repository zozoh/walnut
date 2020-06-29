package org.nutz.walnut.ext.lbs.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
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
        List<NutMap> list = cmd_lbs.getCountries(lang);

        Object re = list;
        if (hc.params.is("ajax")) {
            re = Ajax.ok().setData(list);
        }

        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);
    }

}
