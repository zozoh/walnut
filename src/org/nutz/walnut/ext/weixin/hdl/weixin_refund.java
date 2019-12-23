package org.nutz.walnut.ext.weixin.hdl;

import java.io.InputStream;

import javax.net.ssl.SSLSocketFactory;

import org.nutz.http.Request;
import org.nutz.http.Request.METHOD;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Xmls;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.weixin.WxConf;
import org.nutz.walnut.ext.weixin.WxUtil;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.weixin.util.WxPaySSL;
import org.nutz.weixin.util.Wxs;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 从文件里获取支付签名内容
 * weixin xxx refun id:xxxx
 * 
 * # 其他命令输出里获取支付签名内容
 * echo xxx | weixin xxx refun
 * 
 * # 直接读取支付签名内容
 * weixin xxx refun '{..}'
 * </pre>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(json|c|n|q)$")
public class weixin_refund implements JvmHdl {

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

        // 是否已经支付的单呀
        if (!payMap.containsKey("out_trade_no")) {
            sys.err.print("e.cmd.weixin.refun.miss_out_trade_no");
            return;
        }
        payMap.put("out_refund_no", payMap.get("out_trade_no"));
        if (!payMap.containsKey("total_fee")) {
            sys.err.print("e.cmd.weixin.refun.miss_total_fee");
            return;
        }
        if (!payMap.containsKey("refund_fee")) {
            sys.err.print("e.cmd.weixin.refun.miss_refund_fee");
            return;
        }


        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 然后对 map 签名
        Wxs.fillPayMap(payMap, conf.pay_key);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // https://api.mch.weixin.qq.com/secapi/pay/refund
        // 输出
        String url = "https://api.mch.weixin.qq.com/secapi/pay/refund";
        String xml = Xmls.mapToXml(payMap);
        Request req = Request.create(url, METHOD.POST);
        req.setData(xml);
        Sender sender = Sender.create(req);
        SSLSocketFactory sslSocketFactory;
        try (InputStream ins = sys.io.getInputStream(sys.io.check(wxApi.getHomeObj(), "apiclient_cert.p12"), 0)) {
            sslSocketFactory = WxPaySSL.buildSSL(ins, Strings.sBlank(conf.key_password, conf.pay_mch_id));
        }
        catch (Exception e) {
            throw Lang.wrapThrow(e);
        }
        sender.setSSLSocketFactory(sslSocketFactory);
        Response resp = sender.send();
        if (!resp.isOK())
            throw new IllegalStateException("postPay with SSL, resp code=" + resp.getStatus());
        String re = resp.getContent("UTF-8");
        if (hc.params.is("json")) {
            sys.out.println(Json.toJson(Xmls.xmlToMap(re), hc.jfmt));
        }
        else {
            sys.out.print(re);
        }
    }

}
