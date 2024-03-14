package com.site0.walnut.ext.sys.websocket.hdl;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.site0.walnut.ext.sys.websocket.WsUtils;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

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
