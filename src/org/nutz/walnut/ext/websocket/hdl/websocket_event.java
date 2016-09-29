package org.nutz.walnut.ext.websocket.hdl;

import java.util.Map;

import javax.websocket.Session;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.websocket.WnWebSocket;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.util.ZParams;

public class websocket_event implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
        Session session = WnWebSocket.get(params.val_check(0));
        if (session == null)
            return;
        String event = params.val_check(1);
        Map<String, Object> map = null;
        if (params.vals.length > 2) {
            map = Lang.map(params.val(2));
        }
        NutMap re = new NutMap("event", event);
        if (map != null)
            re.putAll(map);
        String callback = params.get("callback");
        if (!Strings.isBlank(callback)) {
            String id = R.UU32();
            re.put("id", id);
            re.put("user", sys.me.name());
            WnRun.sudo(sys, new Atom() {
                public void run() {
                    WnObj cfile = sys.io.createIfNoExists(null, "/sys/ws/"+id, WnRace.FILE);
                    NutMap meta = new NutMap();
                    meta.put("ws_usr", sys.me.name());
                    meta.put("ws_grp", sys.me.group());
                    meta.put("expi", System.currentTimeMillis() +300*1000);
                    sys.io.writeMeta(cfile, meta);
                }
            });
        }
        session.getAsyncRemote().sendText(Json.toJson(re, JsonFormat.compact()));
    }

}
