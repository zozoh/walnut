package org.nutz.walnut.ext.weixin.hdl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

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
        try {
            // 读取微信配置信息
            WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);

            String url = hc.params.vals[0];
            String state = hc.params.get("state");

            // 得到转发的 url
            String wx_url, scope;
            // 开放平台
            if (hc.params.is("wxopen")) {
                wx_url = "https://open.weixin.qq.com/connect/qrconnect";
                scope = hc.params.get("scope", "snsapi_login");
            }
            // 公众号平台
            else {
                wx_url = "https://open.weixin.qq.com/connect/oauth2/authorize";
                scope = hc.params.get("scope", "snsapi_base");
            }

            String fmt = wx_url
                         + "?appid=%s"
                         + "&redirect_uri=%s"
                         + "&response_type=code"
                         + "&scope=%s"
                         + "%s"
                         + "#wechat_redirect";

            sys.out.println(String.format(fmt,
                                          wxApi.getConfig().appID,
                                          URLEncoder.encode(url, "UTF-8"),
                                          scope,
                                          Strings.isBlank(state) ? "" : "&state=" + state));
        }
        catch (UnsupportedEncodingException e) {
            throw Er.wrap(e);
        }

    }

}
