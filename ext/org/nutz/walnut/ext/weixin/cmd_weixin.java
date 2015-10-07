package org.nutz.walnut.ext.weixin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;
import org.nutz.weixin.bean.WxArticle;
import org.nutz.weixin.bean.WxInMsg;
import org.nutz.weixin.bean.WxOutMsg;
import org.nutz.weixin.spi.WxResp;
import org.nutz.weixin.util.Wxs;

public class cmd_weixin extends JvmExecutor {

    private static final Log log = Logs.get();

    private WeixinIn wxin;

    private WeixinMenu wxmenu;

    private WeixinQrcode wxqrcode;

    public cmd_weixin() {
        wxin = new WeixinIn();
        wxmenu = new WeixinMenu();
        wxqrcode = new WeixinQrcode();
    }

    @Override
    public void exec(final WnSystem sys, String[] args) {
        // 分析参数
        ZParams params = ZParams.parse(args, null);

        // 处理输入
        if (params.has("in")) {
            String str = params.get("in");
            WnObj o = Wn.checkObj(sys, str);
            wxin.handle(sys, o);
        }
        // 处理菜单
        else if (params.has("menu")) {
            wxmenu.handle(sys, params);
        }
        // 处理摇一摇
        else if (params.has("shake")) {
            __do_shake(sys, params);
        }
        // 处理二维码
        else if (params.has("qrcode")) {
            wxqrcode.handle(sys, params);
        }
        // 输出微信的响应消息
        else if (params.has("out")) {
            __do_msg_out(sys, params);
        }
        // 处理 JSAPI 的请求
        else if (params.has("jssdk")) {
            __do_jssdk(sys, params);
        }
        // 准备支付平台的请求
        else if (params.has("pay")) {
            __do_pay(sys, params);
        }
        // 处理支付平台请求的返回
        else if (params.has("payre")) {
            __do_pay_re(sys, params);
        }
        // 输出信息
        else if (params.has("info")) {
            __do_info(sys, params);
        }
        // 无法处理
        else {
            throw Er.create("e.cmd.weixin.invalid", args);
        }

    }

    private void __do_info(WnSystem sys, ZParams params) {
        // 读取微信配置信息
        WnIoWeixinApi wxApi = WxUtil.newWxApi(sys, params);
        WxConf conf = wxApi.getConfig();

        String regex = params.check("info");
        NutMap map = Lang.obj2nutmap(conf);
        String[] keys = map.keySet().toArray(new String[map.size()]);
        for (String key : keys) {
            if (!key.matches(regex)) {
                map.remove(key);
            }
        }

        sys.out.println(Json.toJson(map));
    }

    private static final DateFormat _df = new SimpleDateFormat("yyyyMMddHHmmss");

    private void __do_pay_re(WnSystem sys, ZParams params) {
        // 读取微信配置信息
        WnIoWeixinApi wxApi = WxUtil.newWxApi(sys, params);
        WxConf conf = wxApi.getConfig();

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 首先读取源数据
        String pay = params.get("payre");
        // 从 pipe 读取
        if ("true".equals(pay)) {
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
        NutMap map = Wxs.checkPayReturn(pay, conf.payKey);

        // 输出 JSON 字符串
        sys.out.println(Json.toJson(map));
    }

    private void __do_pay(WnSystem sys, ZParams params) {
        // 读取微信配置信息
        WnIoWeixinApi wxApi = WxUtil.newWxApi(sys, params);
        WxConf conf = wxApi.getConfig();

        // 确保有商户 key
        if (Strings.isBlank(conf.payKey)) {
            throw Er.create("e.cmd.weixin.noPayKey");
        }
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 首先读取源数据
        String pay = params.get("pay");
        NutMap payMap;
        // 从 pipe 读取
        if ("true".equals(pay)) {
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
                payMap.setv("device_info", params.get("dev", "WEB"));
            }

            if (!payMap.has("notify_url")) {
                payMap.setv("notify_url", conf.pay_notify_url);
            }

            if (!payMap.has("trade_type")) {
                payMap.setv("trade_type", params.get("trade_type", "JSAPI"));
            }

            if (!payMap.has("time_expire")) {
                long pay_expired = Math.max(conf.pay_time_expire, 10) * 60 * 1000;
                long start;
                if (payMap.has("time_start")) {
                    Date dStart = Times.parseq(_df, payMap.getString("time_start"));
                    start = dStart.getTime();
                } else {
                    start = System.currentTimeMillis();
                }
                Date d = Times.D(start + pay_expired);
                String ds = Times.format(_df, d);
                payMap.setv("time_expire", ds);
            }
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 然后对 map 签名
        Wxs.fillPayMap(payMap, conf.payKey);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 输出
        if (params.is("json")) {
            sys.out.println(Json.toJson(payMap));
        }
        // 默认按 xml 输出
        else {
            sys.out.println(Xmls.mapToXml(payMap));
        }

    }

    private void __do_jssdk(final WnSystem sys, ZParams params) {
        WnIoWeixinApi wxApi = WxUtil.newWxApi(sys, params);
        WxConf conf = wxApi.getConfig();
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        String url = params.check("jssdk");
        if (!url.startsWith("http"))
            url = conf.jsSdkUrl;

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 确保有 apilist
        String sApi = params.get("apilist");
        String[] jsApiList;

        // 采用默认的 apilist 文件
        if (Strings.isBlank(sApi)) {
            WnObj oApiList = sys.io.check(wxApi.getHomeObj(), "jssdk_api_list");
            String content = sys.io.readText(oApiList);
            jsApiList = Strings.splitIgnoreBlank(content, "[\n,]");
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

        if (log.isDebugEnabled())
            log.debugf("jssdk URL : %s", url);

        // 输出 JS 调用 wx.config({..});
        if (params.is("asjs")) {
            sys.out.print("wx.config(");
            sys.out.print(Json.toJson(map));
            sys.out.print(");");
            sys.out.println();
        }
        // 仅仅输出 JSON
        else {
            sys.out.println(Json.toJson(map));
        }
    }

    private void __do_shake(final WnSystem sys, ZParams params) {
        // 创建微信 API
        WnIoWeixinApi wxApi = WxUtil.newWxApi(sys, params);

        // 执行
        String ticket = params.get("shake");
        WxResp resp = wxApi.getShakeInfo(ticket, 0);

        // 输出
        sys.out.println(Json.toJson(resp, JsonFormat.forLook()));
    }

    private void __do_msg_out(final WnSystem sys, ZParams params) {
        String out = params.get("out");
        WxOutMsg om = null;

        // 一个 JSON
        if (Strings.isQuoteBy(out, '{', '}')) {
            om = Json.fromJson(WxOutMsg.class, out);
        }
        // 一个简单的文本
        else if (out.startsWith("text:")) {
            om = Wxs.respText(null, out.substring("text:".length()));
        }
        // 一篇简单的文章
        else if (out.startsWith("article:")) {
            String[] ss = Strings.splitIgnoreBlank(out.substring("article:".length()), ";;");
            WxArticle arti = new WxArticle();
            arti.setTitle(ss[0]);
            if (ss.length > 1)
                arti.setDescription(ss[1]);
            if (ss.length > 2)
                arti.setUrl(ss[2]);
            om = Wxs.respNews(null, arti);
        }
        // 试图从一个对象里读取文本内容
        else {
            WnObj oMsg = Wn.checkObj(sys, out);
            om = sys.io.readJson(oMsg, WxOutMsg.class);
        }
        // 如果指明了输入源，则视图覆盖 from/toUserName
        String inmsg = params.get("inmsg");
        WnObj oi = null;
        if (!Strings.isBlank(inmsg)) {
            oi = Wn.checkObj(sys, inmsg);
            WxInMsg im = WxUtil.getFromObj(oi);
            Wxs.fix(im, om);
        }
        // 写入标准输出
        String xml = Wxs.asXml(om);
        sys.out.println(xml);
    }

}
