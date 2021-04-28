package org.nutz.walnut.ext.net.weixin.hdl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
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
 * # 从文件里获取支付签名内容
 * weixin xxx pay id:xxxx
 * 
 * # 其他命令输出里获取支付签名内容
 * echo xxx | weixin xxx pay
 * 
 * # 直接读取支付签名内容
 * weixin xxx pay '{..}'
 * </pre>
 * 
 * 默认的命令会输出 xml 内容，你可以直接 post 给微信服务器。如果增加 <code>-json</code> 参数，则会输出成 JSON
 * 内容，以便你查看
 * 
 * 下面这些参数，会覆盖签名文档里面的同名键
 * <ul>
 * <li><b>dev</b> : "WEB" 签名设备（参见微信支付文档）
 * <li><b>trade_type</b> : "JSAPI" 交易方式（参见微信支付文档）
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(json|c|n|q)$")
public class weixin_pay implements JvmHdl {

    private static final DateFormat _df = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 读取微信配置信息
        WnIoWeixinApi wxApi = WxUtil.genWxApi(sys, hc);
        WxConf conf = wxApi.getConfig();

        // 确保有商户 key
        if (Strings.isBlank(conf.pay_key)) {
            throw Er.create("e.cmd.weixin.noPayKey");
        }
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 首先读取源数据
        String pay = hc.params.vals.length > 0 ? hc.params.vals[0] : null;

        NutMap payMap;
        // 从 pipe 读取
        if (Strings.isBlank(pay)) {
            String str = sys.in.readAll();
            payMap = Lang.map(str);
        }
        // 直接是 JSON 字符串
        else if (Strings.isQuoteBy(pay, '{', '}')) {
            payMap = Json.fromJson(NutMap.class, pay);
        }
        // 读一个文件的内容
        else {
            WnObj o = Wn.checkObj(sys, pay);
            payMap = sys.io.readJson(o, NutMap.class);
        }
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 填充 map 的字段
        payMap.setv("appid", conf.appID);

        if (!payMap.has("mch_id")) {
            payMap.setv("mch_id", conf.pay_mch_id);
        }

        // 如果是下单接口，自动检查，看看有没有可以补充的值
        if (payMap.has("total_fee")
            && payMap.has("openid")
            && payMap.has("body")
            && payMap.has("spbill_create_ip")) {
            if (!payMap.has("device_info")) {
                payMap.setv("device_info", hc.params.get("dev", "WEB"));
            }

            if (!payMap.has("notify_url") || hc.params.has("notify_url")) {
                payMap.setv("notify_url", conf.pay_notify_url);
            }

            if (!payMap.has("trade_type") || hc.params.has("trade_type")) {
                payMap.setv("trade_type", hc.params.get("trade_type", "JSAPI"));
            }

            if (!payMap.has("time_expire")) {
                long pay_expired = Math.max(conf.pay_time_expire, 10) * 60 * 1000;
                long start;
                if (payMap.has("time_start")) {
                    Date dStart = Times.parseq(_df, payMap.getString("time_start"));
                    start = dStart.getTime();
                } else {
                    start = Wn.now();
                }
                Date d = Times.D(start + pay_expired);
                String ds = Times.format(_df, d);
                payMap.setv("time_expire", ds);
            }
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 然后对 map 签名
        Wxs.fillPayMap(payMap, conf.pay_key);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 输出
        if (hc.params.is("json")) {
            sys.out.println(Json.toJson(payMap, hc.jfmt));
        }
        // 默认按 xml 输出
        else {
            sys.out.println(Xmls.mapToXml(payMap));
        }
    }

}
