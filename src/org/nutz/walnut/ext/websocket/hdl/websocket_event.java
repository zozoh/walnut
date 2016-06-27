package org.nutz.walnut.ext.websocket.hdl;

import java.util.Map;

import javax.websocket.Session;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.websocket.WnWebSocket;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class websocket_event implements JvmHdl {

    @SuppressWarnings("unchecked")
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        Session session = WnWebSocket.get(params.val_check(0));
        if (session == null)
            return;
        String event = params.val_check(1);
        Map<Object, Object> map = null;
        if (params.vals.length > 2) {
            map = Json.fromJson(Map.class, params.val(2));
        }
        session.getAsyncRemote().sendText(Json.toJson(new NutMap("event", event).setv("params", map), JsonFormat.compact()));
    }

}
