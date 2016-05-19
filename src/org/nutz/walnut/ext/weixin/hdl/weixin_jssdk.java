package org.nutz.walnut.ext.weixin.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.weixin.WxConf;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 采用默认的 JS-SDK 的配置信息
 * # 默认 URL 存在 wxconf.jsSdkUrl
 * # 默认的 api_list 存放在 wxconf.jsApiList 
 * weixin xxx jssdk
 * 
 * # 指定一个 URL 的 JS-SDK 配置信息
 * weixin xxx jssdk http://xxxx
 * 
 * # 指定了 api_list
 * weixin xxx jssdk -apilist ":aa,bb,cc,dd"
 * 
 * # 指定了 api_list (JSON)
 * weixin xxx jssdk -apilist "['aa','bb','cc','dd']
 * 
 * # 指定了 api_list (文件对象)
 * weixin xxx jssdk -apilist id:xxxx
 * 
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(debug|c|n|q)$")
public class weixin_jssdk implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);
        WxConf conf = wxApi.getConfig();

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        String url = hc.params.vals.length > 0 ? hc.params.vals[0] : null;

        if (Strings.isBlank(url))
            url = conf.jsSdkUrl;

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 确保有 apilist
        String sApi = hc.params.get("apilist");
        String[] jsApiList;

        // 采用默认的 apilist 文件
        if (Strings.isBlank(sApi)) {
            jsApiList = conf.jsApiList;
        }
        // 直接列表
        else if (sApi.startsWith(":")) {
            jsApiList = Strings.splitIgnoreBlank(sApi, "[:,]");
        }
        // JSON 列表
        else if (Strings.isQuoteBy(sApi, '[', ']')) {
            jsApiList = Json.fromJson(String[].class, sApi);
        }
        // 某个文件
        else {
            WnObj oApiList = Wn.checkObj(sys, sApi);
            String content = sys.io.readText(oApiList);
            jsApiList = Strings.splitIgnoreBlank(content, "[\n,]");
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        NutMap map = wxApi.genJsSDKConfig(url, jsApiList);

        // 调试模式的话，也输出 URL
        if (hc.params.is("debug")) {
            map.put("url", url);
        }

        // 输出 JSON
        sys.out.println(Json.toJson(map, hc.jfmt));
    }

}
