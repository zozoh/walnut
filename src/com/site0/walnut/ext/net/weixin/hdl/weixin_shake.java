package com.site0.walnut.ext.net.weixin.hdl;

import org.nutz.json.Json;
import com.site0.walnut.ext.net.weixin.WnIoWeixinApi;
import com.site0.walnut.ext.net.weixin.WxUtil;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import org.nutz.weixin.spi.WxResp;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 得到摇一摇信息
 * weixin xxx shake $TICKET
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(c|n|q)$")
public class weixin_shake implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 创建微信 API
        WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);

        // 执行
        String ticket = hc.params.vals[0];
        WxResp resp = wxApi.getShakeInfo(ticket, 0);

        // 输出
        sys.out.println(Json.toJson(resp, hc.jfmt));
    }

}
