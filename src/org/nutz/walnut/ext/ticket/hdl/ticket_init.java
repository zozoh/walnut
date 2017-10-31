package org.nutz.walnut.ext.ticket.hdl;

import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class ticket_init implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        String dest = params.get("d", "~");
        String cmdText = "app init /etc/init/ticket " + dest;
        if (params.has("u")) {
            cmdText += " -u " + params.get("u");
            cmdText += " -c 'ta_unm:\"" + params.get("u") + "\"'";
        }
        sys.exec(cmdText);
    }

}
