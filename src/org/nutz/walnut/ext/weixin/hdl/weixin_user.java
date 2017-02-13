package org.nutz.walnut.ext.weixin.hdl;

import org.nutz.http.Http;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.weixin.WxConf;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.weixin.spi.WxResp;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 根据 OpenID 获取用户信息
 * weixin xxx user -openid xxx
 * 
 * # 指定语言
 * weixin xxx user -openid xxx -lang zh_CN
 * 
 * # 根据 code 获取用户信息(仅OpenId)
 * weixin xxx user -code xxx
 * 
 * # 根据 code 获取用户信息(仅关注者)
 * weixin xxx user -code xxx -infol follower
 * 
 * # 根据 code 获取用户信息(任何人)
 * weixin xxx user -code xxx -infol others
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("cnq")
public class weixin_user implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // 读取微信配置信息
        WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);

        String code = hc.params.get("code");

        // 直接通过 OpenId 获取
        if (Strings.isBlank(code)) {
            String openid = hc.params.check("openid");
            String lang = hc.params.get("lang", "zh_CN");

            WxResp resp = wxApi.user_info(openid, lang);

            sys.out.println(Json.toJson(resp, hc.jfmt));
        }
        // 通过 Code 获取
        else {
            // 读取微信配置信息
            WxConf wxConf = wxApi.getConfig();

            String fmt = "https://api.weixin.qq.com/sns/oauth2/access_token"
                         + "?appid=%s"
                         + "&secret=%s"
                         + "&code=%s"
                         + "&grant_type=authorization_code";

            String url = String.format(fmt, wxConf.appID, wxConf.appsecret, code);

            String json = Http.get(url).getContent();
            NutMap map = Json.fromJson(NutMap.class, json);

            // 如果想进一步获取用户完整的信息
            // 根据参数 infol :
            // - openid: 到此为止
            // - follower: 根据 openid 获取信息
            // - others: 认为本次授权是 "snsapi_userinfo"，则试图根据 refresh_token 获取更多信息
            String infoLevel = hc.params.get("infol", "openid");
            String openid = map.getString("openid");
            String lang = hc.params.get("lang", "zh_CN");
            System.out.println(Json.toJson(map));
            // follower: 根据 openid 获取信息
            if ("follower".equals(infoLevel)) {
                WxResp resp = wxApi.user_info(openid, lang);
                map = resp;
            }
            // others
            else if ("others".equals(infoLevel)) {
                // 根据网页授权access_token获取用户信息
                String web_access_token = map.getString("access_token");
                // String web_refresh_token = map.getString("refresh_token");
                // int web_expires_in = map.getInt("expires_in");
                String uinfoUrl = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=%s",
                                                web_access_token,
                                                openid,
                                                lang);
                String uinfoJson = Http.get(uinfoUrl).getContent();
                NutMap uinfo = Json.fromJson(NutMap.class, uinfoJson);
                map = uinfo;
            }

            // 最后打印结果
            sys.out.println(Json.toJson(map, hc.jfmt));
        }

    }

}
