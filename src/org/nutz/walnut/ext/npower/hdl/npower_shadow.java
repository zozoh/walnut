package org.nutz.walnut.ext.npower.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
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
public class npower_shadow implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnObj conf = sys.io.check(null, Wn.normalizeFullPath("~/.aliyun/" + hc.params.get("cnf", "npower"), sys));
        String imei = hc.params.val_check(0);

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
            if (!hc.params.has("u")) {
                sys.out.print(shadow_str);
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
            desired.putAll(Lang.map(hc.params.get("u")));
            NutMap re = new NutMap();
            re.put("version", version + 1);
            re.put("method", "update");
            re.put("state", new NutMap("desired", desired));
            String n = Json.toJson(re, JsonFormat.full());
            
            // 都准备好了,看看是真更新还是假更新
            if (hc.params.is("dry")) {
                sys.out.print("{'ok':true, 'dry':true}");
                return;
            }
            
            UpdateDeviceShadowRequest req2 = new UpdateDeviceShadowRequest();
            req2.setDeviceName(imei);
            req2.setProductKey(productKey);
            req2.setShadowMessage(n);
            UpdateDeviceShadowResponse resp2 = client.getAcsResponse(req2);
            if (resp2.getSuccess()) {
                sys.out.print("{'ok':true}");
            }
        }
    }

}
