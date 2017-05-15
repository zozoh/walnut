package org.nutz.walnut.ext.payment.alipay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.ext.alipay.AlipayConfig;
import org.nutz.walnut.ext.alipay.AlipayNotify;
import org.nutz.walnut.ext.alipay.AlipaySubmit;
import org.nutz.walnut.ext.payment.WnPay3x;
import org.nutz.walnut.ext.payment.WnPay3xDataType;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPayObj;

/**
 * 支付宝主动扫二维码付款
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 */
public class ZfbQrcodePay3x extends WnPay3x {

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        AlipayConfig alipayConf = getConfig(po);
        
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 填充 map 的字段
        // 把请求参数打包成数组
        Map<String, String> sParaTemp = new LinkedHashMap<>();
        sParaTemp.put("service", "create_direct_pay_by_user");
        sParaTemp.put("partner", alipayConf.partner);
        sParaTemp.put("seller_id", alipayConf.partner);
        sParaTemp.put("_input_charset", "UTF-8");
        sParaTemp.put("payment_type", alipayConf.payment_type);

        // 支付完成(成功或失败均未知)时的回调  TODO 
        if (!Strings.isBlank(alipayConf.pay_notify_url))
            sParaTemp.put("pay_notify_url", alipayConf.pay_notify_url);
        if (!Strings.isBlank(po.getString(WnPayObj.KEY_RETURN_URL)))
            sParaTemp.put("pay_return_url", po.getString("pay_return_url"));
        else if (!Strings.isBlank(alipayConf.pay_return_url))
            sParaTemp.put("pay_return_url", alipayConf.pay_return_url);
        
        sParaTemp.put("anti_phishing_key", "");
        sParaTemp.put("exter_invoke_ip", "");
        sParaTemp.put("out_trade_no", po.getString("out_trade_no", R.UU32()));
        sParaTemp.put("subject", po.getString(WnPayObj.KEY_BRIEF, "测试商品"));
        sParaTemp.put("total_fee", po.getString(WnPayObj.KEY_FEE));
        sParaTemp.put("body", po.getString(WnPayObj.KEY_BRIEF, "测试商品"));
        // 其他业务参数根据在线开发文档，添加参数.文档地址:https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.O9yorI&treeId=62&articleId=103740&docType=1
        // 如sParaTemp.put("参数名","参数值");
        sParaTemp.put("extra_common_param", po.id());

        // 建立请求
        sParaTemp = AlipaySubmit.buildRequestPara(sParaTemp, alipayConf);
        String url = "";

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 输出
        try {
            StringBuilder sb = new StringBuilder(AlipayConfig.ALIPAY_GATEWAY_NEW);
            for (Entry<String, String> en : sParaTemp.entrySet()) {
                sb.append(en.getKey())
                  .append("=")
                  .append(URLEncoder.encode(en.getValue(), "UTF-8"))
                  .append("&");
            }
            sb.setLength(sb.length() - 1);
            url = sb.toString();
        }
        catch (UnsupportedEncodingException e) {}
        
        WnPay3xRe re = new WnPay3xRe();
        re.setDataType(WnPay3xDataType.LINK);
        re.setData(url);
        return re;
    }

    @SuppressWarnings("unchecked")
    @Override
    public WnPay3xRe check(WnPayObj po) {
        AlipayConfig alipayConfig = this.getConfig(po);
        Map<String, String> params = po.getAs("params", Map.class);
        WnPay3xRe re = new WnPay3xRe();
        re.setDataType(WnPay3xDataType.TEXT);
        if (AlipayNotify.verify(params, alipayConfig)) {
            String trade_status = params.get("trade_status");
            re.setData(trade_status);
        } else {
            re.setData("INVAILD");
        }
        return re;
    }

    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        throw Lang.noImplement();
    }
    
    public AlipayConfig getConfig(WnPayObj po) {
        WnUsr seller = run.usrs().check("id:" + po.getString(WnPayObj.KEY_SELLER_ID));
        WnObj conf = io.check(null, seller.home() + "/.payment/conf/alipay");
        return io.readJson(conf, AlipayConfig.class);
    }

}
