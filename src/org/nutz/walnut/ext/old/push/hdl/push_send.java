package org.nutz.walnut.ext.old.push.hdl;

import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.walnut.ext.old.push.XXPushs;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class push_send implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, "v");
        String text = Cmds.checkParamOrPipe(sys, params, "text", true);
        String alias = params.check("r");
        String msgtype = params.get("msgtype", "message");
        String provider = params.get("provider", "jpush");
        String platform = params.get("platform");
        
        String message = "message".equals(msgtype) ? text : null;
        String alert = !"message".equals(msgtype) ? text : null;
        Map<String, String> extras;
        if (params.has("extras")) {
            extras = Json.fromJsonAsMap(String.class, params.get("extras"));
        } else {
            extras = new HashMap<>();
        }

        String re = XXPushs.send(sys, provider, alias, message, alert, extras, platform);
        if (re != null) {
            if (re.startsWith("e.cmd"))
                throw Err.create(re);
            else
                sys.out.print(re);
        }
    }

}
