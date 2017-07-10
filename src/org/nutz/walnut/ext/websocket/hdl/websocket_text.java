package org.nutz.walnut.ext.websocket.hdl;

import org.nutz.walnut.ext.websocket.WsUtils;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.ZParams;

public class websocket_text implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        String text = Cmds.checkParamOrPipe(sys, params, 1);
        WsUtils.eachAsync(sys, params.val_check(0), (async) -> async.sendText(text));
    }

}
