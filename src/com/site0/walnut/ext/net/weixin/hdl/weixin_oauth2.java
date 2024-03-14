package com.site0.walnut.ext.net.weixin.hdl;

import com.site0.walnut.ext.net.weixin.WnIoWeixinApi;
import com.site0.walnut.ext.net.weixin.WxUtil;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 生成重定向请求的 URL
 * weixin xxx oauth2 "http://redirect.com"
 * 
 * # 指定信息获取的级别
 * weixin xxx oauth2 "http://xxx" -scope snsapi_base
 * 
 * # 指定一个状态码
 * weixin xxx oauth2 "http://xxx" -state ANY
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(wxopen)$")
public class weixin_oauth2 implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 读取微信配置信息
        WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);

        String url = hc.params.val_check(0);
        String state = hc.params.get("state");

        // 得到转发的 url
        String wx_url;
        // 开放平台
        if (hc.params.is("wxopen")) {
            wx_url = wxApi.oauth2_wxopen(url, state);
        }
        // 公众号平台
        else {
            wx_url = wxApi.oauth2_wxmp(url, state, hc.params.get("scope"));
        }

        // 输出
        sys.out.println(wx_url);
    }

}
