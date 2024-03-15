package com.site0.walnut.ext.sys.websocket;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.Callback;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

/**
 * 帮助函数集
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class WsUtils {

    public static void eachSession(WnSystem sys, String id, Callback<Session> callback) {
        if (id.startsWith("id:") || id.startsWith("~/")) {
            WnObj wobj = Wn.checkObj(sys, id);
            Wlang.each(wobj.get(WnWebSocket.KEY), (int index, Object ele, Object src) -> {
                Session session = WnWebSocket.get(String.valueOf(ele));
                if (session != null)
                    callback.invoke(session);
            });
        } else {
            Session session = WnWebSocket.get(id);
            if (session != null)
                callback.invoke(session);
        }
    }

    public static void eachAsync(WnSystem sys, String id, Callback<RemoteEndpoint.Async> remote) {
        eachSession(sys, id, (session) -> remote.invoke(session.getAsyncRemote()));
    }

    // 防止实例化
    private WsUtils() {}

}
