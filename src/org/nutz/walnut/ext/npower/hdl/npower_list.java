package org.nutz.walnut.ext.npower.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPager;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.iot.model.v20170420.QueryDeviceRequest;
import com.aliyuncs.iot.model.v20170420.QueryDeviceResponse;
import com.aliyuncs.iot.model.v20170420.QueryDeviceResponse.DeviceInfo;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

@JvmHdlParamArgs(value="cqn",regex="^online$")
public class npower_list implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        WnObj conf = sys.io.check(null, Wn.normalizeFullPath("~/.aliyun/" + hc.params.get("cnf", "npower"), sys));

        String accessKey = conf.getString("accessKey");
        String accessSecret = conf.getString("accessSecret");
        DefaultProfile.addEndpoint("cn-shanghai", "cn-shanghai", "Iot", "iot.cn-shanghai.aliyuncs.com");
        IClientProfile profile = DefaultProfile.getProfile("cn-shanghai", accessKey, accessSecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);

        String productKey = conf.getString("productKey");

        WnPager pager = new WnPager(hc.params);

        QueryDeviceRequest req = new QueryDeviceRequest();
        req.setProductKey(productKey);
        req.setPageSize(pager.pgsz);
        req.setCurrentPage(pager.pn - 1);
        QueryDeviceResponse resp = client.getAcsResponse(req);
        if (resp.getSuccess() != null && resp.getSuccess()) {
            pager.sum_count = resp.getTotal();
            pager.sum_page = resp.getPageCount();
            pager.countPage = true;
            List<NutMap> list = new ArrayList<>();
            for (DeviceInfo dev : resp.getData()) {
                if (hc.params.is("online") && !"ONLINE".equals(dev.getDeviceStatus()))
                    continue;
                Map<String, Object> map = Lang.obj2map(dev);
                map.remove("deviceSecret");
                list.add(new NutMap(map));
            }
            Cmds.output_beans(sys, hc.params, pager, list);
        }
    }

}
