package com.site0.walnut.ext.sys.websocket.hdl;

import com.site0.walnut.ext.sys.websocket.WsUtils;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.ZParams;

public class websocket_text implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        String text = Cmds.checkParamOrPipe(sys, params, 1);
        WsUtils.eachAsync(sys, params.val_check(0), (async) -> async.sendText(text));
    }

}
