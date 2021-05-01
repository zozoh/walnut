package org.nutz.walnut.ext.net.mqttc;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.WnRun;
import org.nutz.web.Webs.Err;

@IocBean(depose = "depose")
public class MqttClientService {

    private static final Log log = Logs.get();

    @Inject
    protected WnIo io;
    
    @Inject
    protected WnRun run;

    protected ConcurrentHashMap<String, ConcurrentHashMap<String, MqttClient>> clients = new ConcurrentHashMap<>();

    public MqttClient get(String userName, String key, boolean reload) {
        if (Strings.isBlank(key))
            key = "default";
        ConcurrentHashMap<String, MqttClient> cs = clients.computeIfAbsent(userName, (name) -> {
            return new ConcurrentHashMap<String, MqttClient>();
        });
        if (reload) {
            MqttClient client = cs.remove(key);
            if (client != null) {
                try {
                    client.close();
                }
                catch (MqttException e) {
                    log.info(e.getMessage(), e);
                }
            }
        }
        return cs.computeIfAbsent(key, (name) -> {
            String path = "/home/" + userName + "/.mqttc/" + name + "/conf";
            WnObj wobj = io.fetch(null, path);
            if (wobj == null) {
                throw Err.create("e.service.mqttc.miss_client_config");
            }
            try {
                String md5 = Lang.md5(userName + "," + name);
                return createMqttConnectOptions(wobj, md5);
            }
            catch (MqttException e) {
                log.info(path, e);
                throw Err.create("e.service.mqttc.MqttException");
            }
        });
    }

    public MqttClient createMqttConnectOptions(WnObj wobj, String clientId) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        NutMap map = io.readJson(wobj, NutMap.class);
        options.setAutomaticReconnect(true);
        options.setKeepAliveInterval(180);
        if (!Strings.isBlank(map.getString("will.topic"))) {
            options.setWill(map.getString("will.topic"), map.getString("will.payload").getBytes(), map.getInt("will.qos", 2), map.getBoolean("will.retained", true));
        }
        // 用户信息不一定存在,也不一定需要,所以需要判断一下是否真的要设置
        if (!Strings.isBlank(map.getString("username"))) {
            options.setUserName(map.getString("username"));
        }
        if (!Strings.isBlank(map.getString("password"))) {
            options.setPassword(map.getString("password").toCharArray());
        }
        options.setCleanSession(map.getBoolean("cleanSession", MqttConnectOptions.CLEAN_SESSION_DEFAULT));
        options.setConnectionTimeout(map.getInt("connectionTimeout", MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT));
        // 事实上, urls的优先级是高于url的
        String[] serverURIs = null;
        if (!Strings.isBlank(map.getString("serverURIs"))) {
            serverURIs = Strings.splitIgnoreBlank(map.getString("serverURIs"), ",");
        } else if (!Strings.isBlank(map.getString("url"))) {
            serverURIs = new String[]{map.getString("url")};
        } else {
            throw Err.create("e.service.mqttc.miss_url");
        }
        // TODO 完成SSL相关的配置
        // 使用Walnut持久化
        MqttClientPersistence persistence = new WnMqttClientPersistence(io, wobj.parent());
        MqttClient mqttClient = new MqttClient(serverURIs[0], clientId, persistence);
        mqttClient.setCallback(new MqttCallbackExtended() {
            
            public void connectComplete(boolean reconnect, String serverURI) {
                if (subs.size() > 0) {
                    for (WnMqttSub sub : subs.values()) {
                        try {
                            get(sub.user, sub.mqttc, false).subscribe(sub.topic, sub);
                        }
                        catch (MqttException e) {
                            log.warn(Json.toJson(sub), e);
                        }
                    }
                }
            }

            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (log.isDebugEnabled())
                    log.debugf("topic=%s, msg=%s", topic, message);
            }

            public void deliveryComplete(IMqttDeliveryToken token) {}

            public void connectionLost(Throwable cause) {
                log.warn("lost connection", cause);
            }
            
            
        });
        mqttClient.connect(options);
        return mqttClient;
    }

    public void depose() {
        clients.forEach((k, v) -> {
            v.values().forEach((client) -> {
                try {
                    client.close();
                }
                catch (MqttException e) {
                    log.debug(e.getMessage(), e);
                }
            });
        });
    }
    
    protected Map<String, WnMqttSub> subs = new HashMap<>();
    
    public void addSub(String user, String mqttc, String handler, String topic) throws MqttException {
        String message_path = "/home/" + user + "/.mqttc/" + mqttc + "/message/";
        String handler_path = "/home/" + user + "/.mqttc/" + mqttc + "/handler/" + handler;
        WnMqttSub sub = new WnMqttSub(user, mqttc, topic, message_path, handler_path, run, io);
        
        get(user, mqttc, false).subscribe(topic, sub);
        subs.put(user + ":" + mqttc + ":" + topic, sub);
    }
}
