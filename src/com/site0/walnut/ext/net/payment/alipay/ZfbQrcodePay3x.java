package com.site0.walnut.ext.net.payment.alipay;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.alipay.AlipayConfig;
import com.site0.walnut.ext.net.alipay.AlipayNotify;
import com.site0.walnut.ext.net.alipay.AlipaySubmit;
import com.site0.walnut.ext.net.payment.WnPay3x;
import com.site0.walnut.ext.net.payment.WnPay3xDataType;
import com.site0.walnut.ext.net.payment.WnPay3xRe;
import com.site0.walnut.ext.net.payment.WnPay3xStatus;
import com.site0.walnut.ext.net.payment.WnPayObj;

/**
 * 支付宝主动扫二维码付款
 * 
 * @author zozoh(zozohtnt@gmail.com)
 * @author wendal(wendal1985@gmail.com)
 */
@SuppressWarnings("unchecked")
public class ZfbQrcodePay3x extends WnPay3x {

    public static final String KEY_alipay_send = "alipay_send";
    public static final String KEY_alipay_result = "alipay_result";

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        WnPay3xRe re = new WnPay3xRe();
        re.setStatus(WnPay3xStatus.WAIT);
        re.setDataType(WnPay3xDataType.IFRAME);
        AlipayConfig alipayConf = getConfig(po);

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // 填充 map 的字段
        // 把请求参数打包成数组
        Map<String, String> sParaTemp = po.getAs(KEY_alipay_send, Map.class);
        if (sParaTemp == null) {
            sParaTemp = new LinkedHashMap<>();
            sParaTemp.put("service", "create_direct_pay_by_user");
            sParaTemp.put("partner", alipayConf.partner);
            sParaTemp.put("seller_id", alipayConf.partner);
            sParaTemp.put("_input_charset", "UTF-8");
            sParaTemp.put("payment_type", alipayConf.payment_type);

            // 支付完成(成功或失败均未知)时的回调 TODO
            if (!Strings.isBlank(alipayConf.pay_notify_url))
                sParaTemp.put("notify_url", alipayConf.pay_notify_url);
            else if (!Strings.isBlank(alipayConf.pay_return_url))
                sParaTemp.put("return_url", alipayConf.pay_return_url);

            String total_fee = "" + po.getFeeInYuan();

            sParaTemp.put("anti_phishing_key", "");
            sParaTemp.put("exter_invoke_ip", "");
            sParaTemp.put("out_trade_no", po.id());
            sParaTemp.put("subject", po.getBrief("测试商品"));
            sParaTemp.put("total_fee", total_fee);
            sParaTemp.put("body", po.getBrief("测试商品"));

            // 自定义前置付款二维码的宽度
            sParaTemp.put("qr_pay_mode", "4");
            sParaTemp.put("qrcode_width", "200");
            // 其他业务参数根据在线开发文档，添加参数.文档地址:https://doc.open.alipay.com/doc2/detail.htm?spm=a219a.7629140.0.0.O9yorI&treeId=62&articleId=103740&docType=1
            // 如sParaTemp.put("参数名","参数值");
            // sParaTemp.put("extra_common_param", po.id());

            // 签名
            sParaTemp = AlipaySubmit.buildRequestPara(sParaTemp, alipayConf);
            po.setv(KEY_alipay_send, sParaTemp);
            re.addChangeKeys(KEY_alipay_send);
        }

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
            re.setData(sb.toString());
        }
        catch (UnsupportedEncodingException e) {}
        return re;
    }

    @Override
    public WnPay3xRe check(WnPayObj po) {
        // AlipayConfig alipayConfig = this.getConfig(po);
        // 暂不实现
        WnPay3xRe re = new WnPay3xRe();
        re.setDataType(po.getReturnType());
        re.setStatus(WnPay3xStatus.WAIT);
        return re;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        AlipayConfig alipayConfig = this.getConfig(po);
        Map<String, String> params = (Map<String, String>) (Map) req;
        WnPay3xRe re = new WnPay3xRe();
        if (AlipayNotify.verify(params, alipayConfig)) {
            String trade_status = params.get("trade_status");
            if ("TRADE_FINISHED".equals(trade_status) || "TRADE_SUCCESS".equals(trade_status)) {
                re.setStatus(WnPay3xStatus.OK);
            } else {
                re.setStatus(WnPay3xStatus.FAIL);
            }
        } else {
            re.setStatus(WnPay3xStatus.FAIL);
        }
        return re;
    }

    public AlipayConfig getConfig(WnPayObj po) {
        WnAccount seller = run.login().checkAccountById(po.getSellerId());
        String aph = seller.getHomePath() + "/.alipay/" + po.getPayTarget() + "/alipayconf";
        WnObj conf = io.check(null, aph);
        return io.readJson(conf, AlipayConfig.class);
    }

}
