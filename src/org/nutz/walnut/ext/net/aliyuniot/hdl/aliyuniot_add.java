package org.nutz.walnut.ext.net.aliyuniot.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.iot.model.v20170420.RegistDeviceRequest;
import com.aliyuncs.iot.model.v20170420.RegistDeviceResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

/**
 * 添加设备
 * 
 * @author wendal
 *
 */
@JvmHdlParamArgs("cqn")
public class aliyuniot_add implements JvmHdl {

    @SuppressWarnings("deprecation")
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnObj conf = sys.io.check(null, Wn.normalizeFullPath("~/.aliyuniot/" + hc.params.get("cnf", "default"), sys));

        String accessKey = conf.getString("accessKey");
        String accessSecret = conf.getString("accessSecret");
        DefaultProfile.addEndpoint("cn-shanghai", "cn-shanghai", "Iot", "iot.cn-shanghai.aliyuncs.com");
        IClientProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKey, accessSecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);

        String productKey = conf.getString("productKey");

        NutMap re = new NutMap();
        // 逐个添加并记录状态
        for (String imei : hc.params.vals) {
            RegistDeviceRequest req = new RegistDeviceRequest();
            req.setDeviceName(imei);
            req.setProductKey(productKey);
            RegistDeviceResponse resp = client.getAcsResponse(req);
            if (resp.getSuccess() != null && resp.getSuccess()) {
                re.put(imei, "ok");
            } else {
                re.put(imei, resp.getErrorMessage());
            }
        }
        sys.out.writeJson(re, Cmds.gen_json_format(hc.params));
    }

}
