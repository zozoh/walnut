package org.nutz.walnut.ext.aliyuniot.hdl;

import java.util.ArrayList;
import java.util.List;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.iot.model.v20180120.QueryDeviceDetailRequest;
import com.aliyuncs.iot.model.v20180120.QueryDeviceDetailResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

@JvmHdlParamArgs("cqn")
public class aliyuniot_stat implements JvmHdl {

    @SuppressWarnings("deprecation")
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnObj conf = sys.io.check(null, Wn.normalizeFullPath("~/.aliyuniot/" + hc.params.get("cnf", "default"), sys));

        String accessKey = conf.getString("accessKey");
        String accessSecret = conf.getString("accessSecret");
        DefaultProfile.addEndpoint("cn-shanghai", "cn-shanghai", "Iot", "iot.cn-shanghai.aliyuncs.com");
        IClientProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKey, accessSecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);

        String productKey = conf.getString("productKey");

        List<QueryDeviceDetailResponse.Data> list = new ArrayList<>();
        for (String imei : hc.params.vals) {
            QueryDeviceDetailRequest req = new QueryDeviceDetailRequest();
            req.setDeviceName(imei);
            req.setProductKey(productKey);
            QueryDeviceDetailResponse resp = client.getAcsResponse(req);
            if (resp.getSuccess() != null && resp.getSuccess()) {
                list.add(resp.getData());
                resp.getData().setDeviceSecret(null); // 不允许传输
            }
        }
        sys.out.print(Json.toJson(list, Cmds.gen_json_format(hc.params)));
    }

}
