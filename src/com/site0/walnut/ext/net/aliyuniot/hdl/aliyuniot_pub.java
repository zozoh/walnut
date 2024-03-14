package com.site0.walnut.ext.net.aliyuniot.hdl;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.iot.model.v20170420.PubRequest;
import com.aliyuncs.iot.model.v20170420.PubResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

@JvmHdlParamArgs("cqn")
public class aliyuniot_pub implements JvmHdl {

    @SuppressWarnings("deprecation")
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnObj conf = sys.io.check(null, Wn.normalizeFullPath("~/.aliyuniot/" + hc.params.get("cnf", "default"), sys));

        String accessKey = conf.getString("accessKey");
        String accessSecret = conf.getString("accessSecret");
        DefaultProfile.addEndpoint("cn-shanghai", "cn-shanghai", "Iot", "iot.cn-shanghai.aliyuncs.com");
        IClientProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKey, accessSecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);

        String productKey = conf.getString("productKey");
        String content = hc.params.check("msg");
        content = Base64.getEncoder().encodeToString(content.getBytes());
        int qos = hc.params.getInt("qos", 0);

        Map<String, PubResponse> re = new HashMap<>();
        for (String imei : hc.params.vals) {
            PubRequest req = new PubRequest();
            req.setTopicFullName("/" + productKey + "/" + imei + "/get");
            req.setProductKey(productKey);
            req.setMessageContent(content);
            req.setQos(qos);
            PubResponse resp = client.getAcsResponse(req);
            re.put(imei, resp);
        }
        sys.out.print(Json.toJson(re, Cmds.gen_json_format(hc.params)));
    }

}
