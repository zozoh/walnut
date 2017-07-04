package org.nutz.walnut.ext.websocket.hdl;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.nutz.walnut.ext.websocket.WsUtils;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class websocket_ping implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        WsUtils.eachSession(sys, params.val_check(0), (session) -> {
            try {
                session.getAsyncRemote().sendPing(ByteBuffer.wrap(new byte[0]));
            }
            catch (IllegalArgumentException | IOException e) {}
        });
    }

}
