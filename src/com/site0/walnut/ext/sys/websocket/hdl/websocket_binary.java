package com.site0.walnut.ext.sys.websocket.hdl;

import java.nio.ByteBuffer;

import org.nutz.lang.Streams;
import com.site0.walnut.ext.sys.websocket.WsUtils;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class websocket_binary implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        byte[] buf = Streams.readBytesAndClose(sys.in.getInputStream());
        WsUtils.eachAsync(sys, params.val_check(0), (async) -> async.sendBinary(ByteBuffer.wrap(buf)));
    }

}
