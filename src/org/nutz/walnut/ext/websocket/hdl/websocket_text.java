package org.nutz.walnut.ext.websocket.hdl;

import javax.websocket.Session;

import org.nutz.walnut.ext.websocket.WnWebSocket;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class websocket_text implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        Session session = WnWebSocket.get(params.val_check(0));
        if (session == null)
            return;
        session.getAsyncRemote().sendText(params.val_check(1));
    }

}
