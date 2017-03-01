package org.nutz.walnut.ext.websocket.hdl;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.util.Callback;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.websocket.WnWebSocket;
import org.nutz.walnut.impl.box.WnSystem;

public abstract class websocket_common {
    
    public void eachSession(WnSystem sys, String id, Callback<Session> callback) {
        if (id.startsWith("id:")) {
            id = id.substring(3);
            WnObj wobj = sys.io.checkById(id);
            Lang.each(wobj.get(WnWebSocket.KEY), new Each<Object>() {
                public void invoke(int index, Object ele, int length){
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
    
    public void eachAsync(WnSystem sys, String id, Callback<RemoteEndpoint.Async> remote) {
        eachSession(sys, id, (session)->remote.invoke(session.getAsyncRemote()));
    }
}
