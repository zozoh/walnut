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
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.WnRun;

@ServerEndpoint(value="/websocket", configurator=WnWebSocketConfigurator.class)
@SuppressWarnings("unchecked")
@IocBean(create="init")
public class WnWebSocket extends Endpoint {
    
    private static final Log log = Logs.get();
    
    protected static Map<String, Session> peers = Collections.synchronizedMap(new HashMap<>());
    
    public static String KEY = "websocket_watch";
    
    @Inject
    protected WnRun wnRun;
    
    protected Field idField;
    
    protected WnObj root;

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
                if (obj.has(KEY) && obj.get(KEY) != null && obj.get(KEY) instanceof String)
                    wnRun.io().appendMeta(obj, "{"+KEY+":[]}");//移除老数据
                wnRun.io().push(obj.id(), KEY, session.getId(), false);
                if (doReturn)
                    session.getAsyncRemote().sendText(Json.toJson(new NutMap("event", "watched").setv("obj", obj.id()), JsonFormat.compact()));
                break;
            case "resp":
                String id = map.getString("id");
                if (Strings.isBlank(id) || id.contains(".."))
                    break;
                WnObj cfile = wnRun.io().fetch(root, id);
                if (cfile == null) {
                    log.debug("not such websocket callback file id=" + id);
                    break;
                }
                String callback = wnRun.io().readText(cfile);
                if (Strings.isBlank(callback)) {
                    log.debug("websocket callback file is emtry id=" + id);
                    break;
                }
                String ws_usr = map.getString("ws_usr");
                if (Strings.isBlank(ws_usr)) {
                    log.debug("websocket callback file without ws_usr id=" + id);
                    break;
                }
                Tmpl tmpl = Tmpl.parse(callback);
                NutMap ctx = new NutMap();
                ctx.put("ok", map.getBoolean("ok", false));
                ctx.put("args", map.getList("args", Object.class));
                ctx.put("cfile", cfile);
                String cmd = tmpl.render(ctx);
                wnRun.exec("websocket", ws_usr, cmd);
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
        WnQuery query = new WnQuery();
        query.setv(KEY, session.getId());
        wnRun.io().pull(query, KEY, session.getId());
    }
    
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        peers.remove(session.getId());
        WnQuery query = new WnQuery();
        query.setv(KEY, session.getId());
        wnRun.io().pull(query, KEY, session.getId());
    }
    
    public static Session get(String id) {
        return peers.get(id);
    }
    
    public void init() {
        root = wnRun.io().createIfNoExists(null, "/sys/ws", WnRace.DIR);
    }
}
