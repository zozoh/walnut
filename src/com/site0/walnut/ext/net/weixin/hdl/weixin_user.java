package com.site0.walnut.ext.net.weixin.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;
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

        // 准备参数和返回值
        String openid = hc.params.get("openid");
        String code = hc.params.get("code");
        String lang = hc.params.get("lang");
        String codeType = hc.params.get("type", "gh");
        String infoLevel = hc.params.get("infol");
        WxResp resp;

        // 直接通过 OpenId 获取
        if (!Strings.isBlank(openid)) {
            resp = wxApi.user_info(openid, lang);
        }
        // 通过 Code 获取
        else if (!Strings.isBlank(code)) {
            // follower: 根据 openid 获取信息
            if ("follower".equals(infoLevel)) {
                if ("mp".equals(codeType)) {
                    openid = wxApi.user_openid_by_mp_code(codeType);
                } else {
                    openid = wxApi.user_openid_by_gh_code(code);
                }
                resp = wxApi.user_info(openid, lang);
            }
            // - others: 认为本次授权是 "snsapi_userinfo"，则试图根据 refresh_token 获取更多信息
            else if ("others".equals(infoLevel)) {
                resp = wxApi.user_info_by_code(code, lang);
            }
            // 默认的，就是仅仅获取 openid 咯
            else {
                if ("mp".equals(codeType)) {
                    resp = wxApi.user_info_by_mp_code(code);
                } else {
                    resp = wxApi.user_info_by_gh_code(code);
                }
            }
        }
        // 什么都没有，那么抛错
        else {
            throw Er.create("e.cmd.weixin.user.need_openid_or_code");
        }

        // 最后打印结果
        sys.out.println(Json.toJson(resp, hc.jfmt));

    }

}
