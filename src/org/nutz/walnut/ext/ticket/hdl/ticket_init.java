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

        // 将自己加入到客服中
        // String meid = sys.exec2("me id");
        // String re = sys.exec2f("ticket people -add %s -tp cservice",
        // meid.trim());
        // sys.exec("touch ~/.ticket_client_cservice");
        String re = sys.exec2("ticket my -reg -tp cservice");
        sys.out.print(re);
    }

}
