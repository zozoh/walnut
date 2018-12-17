package org.nutz.walnut.ext.aliyuniot.hdl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.WnOutputable;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.iot.model.v20170420.GetDeviceShadowRequest;
import com.aliyuncs.iot.model.v20170420.GetDeviceShadowResponse;
import com.aliyuncs.iot.model.v20170420.UpdateDeviceShadowRequest;
import com.aliyuncs.iot.model.v20170420.UpdateDeviceShadowResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

/**
 * 获取或更新设备影子数据
 * 
 * @author wendal
 *
 */
@JvmHdlParamArgs(value="cqn", regex="^dry$")
public class aliyuniot_shadow implements JvmHdl {
    
    private static final Log log = Logs.get();
    
    public static ExecutorService es = Executors.newFixedThreadPool(16, new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "aliyuniot_shadow-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        }
    });

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnObj conf = sys.io.check(null, Wn.normalizeFullPath("~/.aliyuniot/" + hc.params.get("cnf", "default"), sys));
        String imei = hc.params.val_check(0);
        String update = hc.params.get("u");
        int delay = hc.params.getInt("delay", 0);
        boolean dry = hc.params.is("dry");
        if (delay < 1) {
            execute(conf, imei, update, sys.out, dry);
        }
        else {
            es.submit(()-> {
                try {
                    execute(conf, imei, update, null, dry);
                }
                catch (Throwable e) {
                    log.debug("something happen", e);
                }
            });
        }
    }

    public void execute(WnObj conf, String imei, String update, WnOutputable out, boolean dry) throws Exception {
        String accessKey = conf.getString("accessKey");
        String accessSecret = conf.getString("accessSecret");
        DefaultProfile.addEndpoint("cn-shanghai", "cn-shanghai", "Iot", "iot.cn-shanghai.aliyuncs.com");
        IClientProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKey, accessSecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);

        String productKey = conf.getString("productKey");

        GetDeviceShadowRequest req = new GetDeviceShadowRequest();
        req.setDeviceName(imei);
        req.setProductKey(productKey);
        GetDeviceShadowResponse resp = client.getAcsResponse(req);
        if (resp.getSuccess() != null && resp.getSuccess()) {
            String shadow_str = resp.getShadowMessage();
            if (update == null) {
                if (out != null)
                    out.print(shadow_str);
                return;
            }
            if (shadow_str == null)
                shadow_str = "{}";
            NutMap shadow = Json.fromJson(NutMap.class, shadow_str);
            if (shadow == null)
                shadow = new NutMap("version", 0);
            int version = shadow.getInt("version");
            NutMap desired = shadow.getAs("desired", NutMap.class);
            if (desired == null)
                desired = new NutMap();
            desired.putAll(Lang.map(update));
            NutMap re = new NutMap();
            re.put("version", version + 1);
            re.put("method", "update");
            re.put("state", new NutMap("desired", desired));
            String n = Json.toJson(re, JsonFormat.full());
            
            // 都准备好了,看看是真更新还是假更新
            if (dry) {
                if (out != null)
                    out.print("{'ok':true, 'dry':true}");
                return;
            }
            
            UpdateDeviceShadowRequest req2 = new UpdateDeviceShadowRequest();
            req2.setDeviceName(imei);
            req2.setProductKey(productKey);
            req2.setShadowMessage(n);
            UpdateDeviceShadowResponse resp2 = client.getAcsResponse(req2);
            if (resp2.getSuccess()) {
                if (out != null)
                    out.print("{'ok':true}");
            }
        }
    }
}
