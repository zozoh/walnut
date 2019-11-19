package org.nutz.walnut.ext.weixin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.nutz.http.Http;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.weixin.impl.WxApi2Impl;
import org.nutz.weixin.spi.WxResp;

/**
 * 对 WxApi 的扩展，并且根据 walnut 的特性做了扩展
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnIoWeixinApi extends WxApi2Impl {

    private WnObj oConf;

    private WnObj oHome;

    private WxConf config;

    public WnIoWeixinApi(WnIo io, WnObj oConf) {
        if (!oConf.isFILE()) {
            throw Er.create("e.wxapi.noconf", oConf);
        }

        // 读取配置文件
        // 从 "~/.weixin/wxconf" 对象中读取必要的信息
        this.config = io.readJson(oConf, WxConf.class);

        // 配置文件所在的目录为主目录
        this.oHome = oConf.parent();
        this.oConf = oConf;

        // 设置 token 存取方式
        this.setAccessTokenStore(new WnIoWxAccessTokenStore(io, oHome));
        this.setJsapiTicketStore(new WnIoWxJsapiTicketStore(io, oHome));

        this.appid = config.appID;
        this.appsecret = config.appsecret;
        this.token = config.token;

    }

    /**
     * 根据用户的票据获取 openid（小程序）
     * 
     * @param code
     *            票据信息
     * @return 用户的 openid
     */
    public String user_openid_by_mp_code(String code) {
        WxResp map = this.user_info_by_mp_code(code);
        return map.getString("openid");
    }

    /**
     * 根据用户的票据获取 openid 等信息集合（小程序）
     * 
     * @param code
     *            票据信息
     * @return 包括 openid 和 access_token
     */
    public WxResp user_info_by_mp_code(String code) {
        String fmt = "https://api.weixin.qq.com/sns/jscode2session"
                     + "?appid=%s"
                     + "&secret=%s"
                     + "&js_code=%s"
                     + "&grant_type=authorization_code";

        String url = String.format(fmt, config.appID, config.appsecret, code);
        return __get_resp(url);
    }

    /**
     * 根据用户的票据获取 openid（公众号）
     * 
     * @param code
     *            票据信息
     * @return 用户的 openid
     */
    public String user_openid_by_gh_code(String code) {
        WxResp map = this.user_info_by_gh_code(code);
        return map.getString("openid");
    }

    /**
     * 根据用户的票据获取 openid 等信息集合（公众号）
     * 
     * @param code
     *            票据信息
     * @return 包括 openid 和 access_token
     */
    public WxResp user_info_by_gh_code(String code) {
        String fmt = "https://api.weixin.qq.com/sns/oauth2/access_token"
                     + "?appid=%s"
                     + "&secret=%s"
                     + "&code=%s"
                     + "&grant_type=authorization_code";

        String url = String.format(fmt, config.appID, config.appsecret, code);
        return __get_resp(url);
    }

    private WxResp __get_resp(String url) {
        String json = Http.get(url).getContent();
        return Json.fromJson(WxResp.class, json);
    }

    /**
     * 根据网页授权access_token获取用户信息
     * 
     * @param code
     *            票据信息。必须用 <code>snsapi_userinfo</code> 级别来获取 code，否则没权限
     * @param lang
     *            语言，默认 "zh_CN"
     * @return 用户的 openid
     */
    public WxResp user_info_by_code(String code, String lang) {
        lang = Strings.sBlank("lang", "zh_CN");
        WxResp map = this.user_info_by_gh_code(code);
        String openid = map.getString("openid");
        String web_access_token = map.getString("access_token");

        String url = String.format("https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=%s",
                                   web_access_token,
                                   openid,
                                   lang);
        return this.__get_resp(url);
    }

    /**
     * 生成微信开放平台的重定向 url，以便获取 `code`
     * 
     * @param url
     *            要重定向的 URL
     * @param state
     *            场景，默认为 null
     * @return 重定向url
     */
    public String oauth2_wxopen(String url, String state) {
        try {
            String wx_url = "https://open.weixin.qq.com/connect/qrconnect";
            String scope = "snsapi_login";

            String fmt = wx_url
                         + "?appid=%s"
                         + "&redirect_uri=%s"
                         + "&response_type=code"
                         + "&scope=%s"
                         + "%s"
                         + "#wechat_redirect";

            return String.format(fmt,
                                 this.config.appID,
                                 URLEncoder.encode(url, "UTF-8"),
                                 scope,
                                 Strings.isBlank(state) ? "" : "&state=" + state);
        }
        catch (UnsupportedEncodingException e) {
            throw Er.wrap(e);
        }
    }

    /**
     * 生成微信公众号平台的重定向 url，以便获取 `code`
     * 
     * @param url
     *            要重定向的 URL
     * @param state
     *            场景，默认为 null
     * @param scope
     *            获取信息级别，默认 snsapi_base
     *            <ul>
     *            <li><code>snsapi_base</code> 仅能获取 openid
     *            <li><code>snsapi_userinfo</code> 获取用户的基本信息的。但这种授权需要用户手动同意，无需关注
     *            </ul>
     * @return 重定向url
     */
    public String oauth2_wxmp(String url, String state, String scope) {
        try {
            String wx_url = "https://open.weixin.qq.com/connect/oauth2/authorize";
            scope = Strings.sBlank(scope, "snsapi_base");

            String fmt = wx_url
                         + "?appid=%s"
                         + "&redirect_uri=%s"
                         + "&response_type=code"
                         + "&scope=%s"
                         + "%s"
                         + "#wechat_redirect";

            return String.format(fmt,
                                 this.config.appID,
                                 URLEncoder.encode(url, "UTF-8"),
                                 scope,
                                 Strings.isBlank(state) ? "" : "&state=" + state);
        }
        catch (UnsupportedEncodingException e) {
            throw Er.wrap(e);
        }
    }

    public WxConf getConfig() {
        return config;
    }

    public WnObj getHomeObj() {
        return oHome;
    }

    public WnObj getConfObj() {
        return oConf;
    }

}
