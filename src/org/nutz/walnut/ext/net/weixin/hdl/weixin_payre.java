package org.nutz.walnut.ext.net.weixin.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.net.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.net.weixin.WxConf;
import org.nutz.walnut.ext.net.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.weixin.util.Wxs;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 检查签名并输出
 * weixin xxx payre id:xxxx
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(get|del|c|n|q)$")
public class weixin_payre implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 读取微信配置信息
        WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);
        WxConf conf = wxApi.getConfig();

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 首先读取源数据
        String pay = hc.params.vals.length > 0 ? hc.params.vals[0] : null;
        // 从 pipe 读取
        if (Strings.isBlank(pay) && sys.pipeId > 0) {
            pay = sys.in.readAll();
        }
        // 直接是 XML 字符串
        else if (Strings.isQuoteBy(pay, "<xml>", "</xml>")) {
            // 啥也不做
        }
        // 读一个文件的内容
        else {
            WnObj o = Wn.checkObj(sys, pay);
            pay = sys.io.readText(o);
        }

        // 检查签名
        NutMap map = Wxs.checkPayReturn(pay, conf.pay_key);

        // 输出 JSON 字符串
        sys.out.println(Json.toJson(map, hc.jfmt));
    }

}
