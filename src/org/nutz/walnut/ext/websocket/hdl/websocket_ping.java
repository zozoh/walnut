package org.nutz.walnut.ext.websocket.hdl;

import java.nio.ByteBuffer;

import javax.websocket.Session;

import org.nutz.walnut.ext.websocket.WnWebSocket;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;
import org.nutz.web.Webs.Err;

public class websocket_ping implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        Session session = WnWebSocket.get(params.val_check(0));
        if (session == null)
            return;
        try {
            session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[0]));
        }
        catch (Exception e) {
            throw Err.create("e.cmd.websocket.ping.fail", e);
        }
    }

}
