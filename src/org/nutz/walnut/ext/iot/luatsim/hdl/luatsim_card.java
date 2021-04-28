package org.nutz.walnut.ext.iot.luatsim.hdl;

import org.nutz.http.Request;
import org.nutz.http.Request.METHOD;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

public class luatsim_card implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String url = "http://api.openluat.com/sim/iotcard/card?";
        String tmp = hc.params.val_check(0);
        if (tmp.length() == 20) {
            url += "iccid=" + tmp;
        }
        else {
            url += "imei=" + tmp;
        }
        String apikey = hc.params.get("key", "ht27EiFyhxGONoA9");
        String apiSecret = hc.params.get("secret", "ZBOstiKVjTWAEtOFz7GxJUhEf2cMHwR6fMQRRrCGBFeJ773ZwoiJxNI1GoWe5itQ");
        Request req = Request.create(url, METHOD.POST);
        req.getHeader().clear();
        req.getHeader().asJsonContentType();
        req.basicAuth(apikey, apiSecret);
        req.header("Accept", "*/*");
        req.setData("{}");
        Response resp = Sender.create(req).setTimeout(5000).call();
        NutMap re = Json.fromJson(NutMap.class, resp.getReader());
        sys.out.writeJson(re, Cmds.gen_json_format(hc.params));
    }

}
