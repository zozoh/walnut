package org.nutz.walnut.ext.iot.luatsim.hdl;

import org.nutz.http.Request;
import org.nutz.http.Request.METHOD;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class luatsim_billgroup implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String url = "http://api.openluat.com/sim/iotcard/billing_group?";
        String apikey = hc.params.get("key", "ht27EiFyhxGONoA9");
        String apiSecret = hc.params.get("secret", "ZBOstiKVjTWAEtOFz7GxJUhEf2cMHwR6fMQRRrCGBFeJ773ZwoiJxNI1GoWe5itQ");
        Request req = Request.create(url, METHOD.POST);
        req.getHeader().clear();
        req.getHeader().asJsonContentType();
        req.basicAuth(apikey, apiSecret);
        req.header("Accept", "*/*");
        req.setData("{}");
        Response resp = Sender.create(req).setTimeout(5000).call();
        sys.out.writeAndClose(resp.getStream());;
    }

}
