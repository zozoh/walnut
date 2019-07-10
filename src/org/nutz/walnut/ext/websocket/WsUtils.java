package org.nutz.walnut.ext.websocket;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.websocket.WnWebSocket;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 帮助函数集
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class WsUtils {

    public static void eachSession(WnSystem sys, String id, Callback<Session> callback) {
        if (id.startsWith("id:") || id.startsWith("~/")) {
            WnObj wobj = Wn.checkObj(sys, id);
            Lang.each(wobj.get(WnWebSocket.KEY), new Each<Object>() {
                public void invoke(int index, Object ele, int length) {
                    Session session = WnWebSocket.get(String.valueOf(ele));
                    if (session != null)
                        callback.invoke(session);
                }
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
