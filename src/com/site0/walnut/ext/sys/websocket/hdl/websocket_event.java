package com.site0.walnut.ext.sys.websocket.hdl;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.trans.Atom;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.sys.websocket.WsUtils;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnRun;
import com.site0.walnut.util.ZParams;

public class websocket_event implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) {
        ZParams params = ZParams.parse(hc.args, null);
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
            re.put("user", sys.getMyName());
            WnRun.sudo(sys, new Atom() {
                public void run() {
                    WnObj cfile = sys.io.createIfNoExists(null, "/sys/ws/" + id, WnRace.FILE);
                    NutMap meta = new NutMap();
                    meta.put("ws_usr", sys.getMyName());
                    meta.put("ws_grp", sys.getMyGroup());
                    meta.put("expi", Wn.now() + 300 * 1000);
                    sys.io.writeMeta(cfile, meta);
                }
            });
        }
        JsonFormat jfmt = JsonFormat.compact().setQuoteName(true);
        String json = Json.toJson(re, jfmt);
        WsUtils.eachAsync(sys, params.val_check(0), (async) -> {
            async.sendText(json);
        });
    }

}
