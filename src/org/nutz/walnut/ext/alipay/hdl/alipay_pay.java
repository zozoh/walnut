package org.nutz.walnut.ext.alipay.hdl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.alipay.AlipayConfig;
import org.nutz.walnut.ext.alipay.AlipaySubmit;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 命令的使用方法:
 * 
 * cat payment | alipay xxx pay
 * 
 */
@JvmHdlParamArgs("^(json|c|n|q)$")
public class alipay_pay implements JvmHdl {

    public static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 读取支付宝配置信息
        WnObj tmp = hc.getAs("alipayconf_obj", WnObj.class);
        AlipayConfig alipayConfig = sys.io.readJson(tmp, AlipayConfig.class);

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
        // 把请求参数打包成数组
        Map<String, String> sParaTemp = new LinkedHashMap<>();
        sParaTemp.put("service", "create_direct_pay_by_user");
        sParaTemp.put("partner", alipayConfig.partner);
        sParaTemp.put("seller_id", alipayConfig.partner);
        sParaTemp.put("_input_charset", "UTF-8");
        sParaTemp.put("payment_type", alipayConfig.payment_type);
        sParaTemp.put("notify_url",
                      payMap.getString("notify_url",
                                       hc.params.get("notify_url",
                                                     "http://wendal.ngrok.wendal.cn/api/root/alipay/payre")));
        if (payMap.has("return_url"))
            sParaTemp.put("return_url", payMap.getString("return_url"));
        else if (hc.params.has("return_url"))
            sParaTemp.put("return_url", hc.params.get("return_url"));
        sParaTemp.put("anti_phishing_key", "");
        sParaTemp.put("exter_invoke_ip", "");
        sParaTemp.put("out_trade_no", payMap.getString("out_trade_no", R.UU32()));
        sParaTemp.put("subject", payMap.getString("subject", "测试商品"));
        sParaTemp.put("total_fee",
                      payMap.getString("total_fee", hc.params.get("total_fee", "0.01")));
        sParaTemp.put("body", payMap.getString("body", hc.params.get("body", "一分钱一分钱")));
        // 其他业务参数根据在线开发文档，添加参数.文档地址:https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.O9yorI&treeId=62&articleId=103740&docType=1
        // 如sParaTemp.put("参数名","参数值");

        // 建立请求
        sParaTemp = AlipaySubmit.buildRequestPara(sParaTemp, alipayConfig);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 输出
        if (hc.params.is("json")) {
            sys.out.println(Json.toJson(sParaTemp, hc.jfmt));
        }
        // 按 重定向Http 输出
        else {
            try {
                StringBuilder sb = new StringBuilder(ALIPAY_GATEWAY_NEW);
                for (Entry<String, String> en : sParaTemp.entrySet()) {
                    sb.append(en.getKey())
                      .append("=")
                      .append(URLEncoder.encode(en.getValue(), "UTF-8"))
                      .append("&");
                }
                sb.setLength(sb.length() - 1);
                sys.out.println("HTTP/1.1 302 Found");
                sys.out.println("Location: " + sb);
            }
            catch (UnsupportedEncodingException e) {}
        }
    }

}
