package org.nutz.walnut.ext.websocket;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.util.WnRun;

@ServerEndpoint(value="/websocket", configurator=WnWebSocketConfigurator.class)
@SuppressWarnings("unchecked")
@IocBean
public class WnWebSocket extends Endpoint {
    
    private static final Log log = Logs.get();
    
    static Map<String, Session> peers = Collections.synchronizedMap(new HashMap<>());
    
    @Inject
    protected WnRun wnRun;
    
    protected Field idField;

    @OnOpen
    public void onOpen(final Session session, EndpointConfig config) {
        try {
            if (idField == null) {
                idField = session.getClass().getDeclaredField("id");
                idField.setAccessible(true);
            }
            idField.set(session, R.UU32());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        peers.put(session.getId(), session);
        session.getAsyncRemote().sendText("{event:'hi','wsid':'"+session.getId()+"'}");
    }
    
    @OnMessage
    public void onMessage(Session session, Reader r) {
        try {
            NutMap map = Json.fromJson(NutMap.class, r);
            log.info("rev " + Json.toJson(map, JsonFormat.compact()));
            if (map == null)
                return;
            String methodName = map.getString("method");
            if (Strings.isBlank(methodName))
                return;
            String user = map.getString("user");
            boolean doReturn = map.getBoolean("return", true);
            switch (methodName) {
            case "watch":
                WnQuery query = new WnQuery();
                query.setAll(map.getAs("match", Map.class));
                query.setv("d0", "home").setv("d1", user);
                query.limit(1);
                
                List<WnObj> tmp = wnRun.io().query(query);
                if (tmp.isEmpty())
                    return;
                WnObj obj = tmp.get(0);
                wnRun.io().appendMeta(obj, new NutMap("websocket_watch", session.getId()));
                if (doReturn)
                    session.getAsyncRemote().sendText(Json.toJson(new NutMap("event", "watched").setv("obj", obj.id()), JsonFormat.compact()));
                break;

            default:
                log.info("unknown method="+methodName);
                break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @OnError
    public void onError(Session session, Throwable thr) {
        peers.remove(session.getId());
    }
    
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        peers.remove(session.getId());
    }
    
    public static Session get(String id) {
        return peers.get(id);
    }
}
