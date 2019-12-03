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

import org.nutz.el.El;
import org.nutz.el.opt.RunMethod;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.WnRun;

@ServerEndpoint(value = "/websocket", configurator = WnWebSocketConfigurator.class)
@IocBean(create = "init")
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

        NutMap map = Lang.map("event", "hi").setv("wsid", session.getId());
        String json = Json.toJson(map, JsonFormat.compact().setQuoteName(true));

        session.getAsyncRemote().sendText(json);
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
            if (Strings.isBlank(user))
                user = "root";

            // 默认需要返回内容
            boolean doReturn = map.getBoolean("return", true);

            // 来吧 ...
            switch (methodName) {
            case "watch":
                NutMap match = map.getAs("match", NutMap.class);
                WnQuery query = new WnQuery();
                query.setAll(match);
                // 没有指定 ID 的话，则需要添加一下 d0/d1 约束，以防止数据集过多
                if (!match.has("id"))
                    query.setv("d0", "home").setv("d1", user);

                // 找一下对象
                WnObj obj = wnRun.io().getOne(query);

                // 木有
                if (null == obj)
                    return;

                // 记录监视的 ws 句柄
                if (obj.has(KEY) && obj.get(KEY) != null && obj.get(KEY) instanceof String)
                    wnRun.io().appendMeta(obj, "{" + KEY + ":[]}");// 移除老数据
                wnRun.io().push(obj.id(), KEY, session.getId(), false);

                // 返回内容
                if (doReturn) {
                    NutMap eventData = new NutMap("event", "watched").setv("obj", obj.id());
                    String eventJson = Json.toJson(eventData,
                                                   JsonFormat.compact().setQuoteName(true));
                    session.getAsyncRemote().sendText(eventJson);
                }
                break;
            case "resp": {
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
            }
            case "cmd": {
                WnAccount usr = wnRun.auth().getAccount(user);
                if (usr == null) {
                    log.debugf("not such websocket user=%s", user);
                    break;
                }
                String cmdpath = map.getString("cmd");
                if (Strings.isBlank(cmdpath) || cmdpath.contains("..") || cmdpath.contains("/")) {
                    log.debugf("invaild websocket cmd path user=%s cmd=%s", user, cmdpath);
                    break;
                }
                WnObj wsroot = wnRun.io().fetch(null, usr.getHomePath() + "/.ws/cmd/");
                if (wsroot == null) {
                    log.debugf("not such websocket callback file user=%s cmd=%s", user, cmdpath);
                    break;
                }

                WnObj cfile = wnRun.io().fetch(wsroot, cmdpath);
                if (cfile == null) {
                    log.debugf("not such websocket callback file user=%s cmd=%s", user, cmdpath);
                    break;
                }
                String callback = wnRun.io().readText(cfile);
                if (Strings.isBlank(callback)) {
                    log.debugf("websocket callback file is emtry user=%s cmd=%s", user, cmdpath);
                    break;
                }
                NutMap ctx = new NutMap();
                ctx.put("ok", map.getBoolean("ok", false));
                ctx.put("args", map.get("args"));
                ctx.put("cfile", cfile);
                String cmd = El.render(callback, Lang.context(ctx));
                wnRun.exec("websocket", user, cmd);
                break;
            }
            default:
                log.info("unknown method=" + methodName);
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
        El.register("tojson", new RunMethod() {
            public Object run(List<Object> fetchParam) {
                return Json.toJson(fetchParam.get(0), JsonFormat.compact());
            }

            public String fetchSelf() {
                return "tojson";
            }
        });
    }
}
