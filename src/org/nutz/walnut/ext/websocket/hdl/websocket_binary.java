package org.nutz.walnut.ext.websocket.hdl;

import java.nio.ByteBuffer;

import org.nutz.lang.Streams;
import org.nutz.walnut.ext.websocket.WsUtils;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class websocket_binary implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        byte[] buf = Streams.readBytesAndClose(sys.in.getInputStream());
        WsUtils.eachAsync(sys, params.val_check(0), (async) -> async.sendBinary(ByteBuffer.wrap(buf)));
    }

}
