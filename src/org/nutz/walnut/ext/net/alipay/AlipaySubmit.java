package org.nutz.walnut.ext.net.alipay;

import java.util.Map;

/** 
 * 
 */
public class AlipaySubmit {

    /**
     * 生成签名结果
     * 
     * @param sPara
     *            要签名的数组
     * @return 签名结果字符串
     */
    public static String buildRequestMysign(Map<String, String> sPara, AlipayConfig config) {
        String prestr = AlipayCore.createLinkString(sPara); // 把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
        String mysign = "";
        // if(AlipayConfig.sign_type.equals("MD5") ) {
        mysign = AlipayMD5.sign(prestr, config.key, "UTF-8");
        // }
        return mysign;
    }

    /**
     * 生成要请求给支付宝的参数数组
     * 
     * @param sParaTemp
     *            请求前的参数数组
     * @return 要请求的参数数组
     */
    public static Map<String, String> buildRequestPara(Map<String, String> sParaTemp,
                                                       AlipayConfig config) {
        // 除去数组中的空值和签名参数
        Map<String, String> sPara = AlipayCore.paraFilter(sParaTemp);
        // 生成签名结果
        String mysign = buildRequestMysign(sPara, config);

        // 签名结果与签名方式加入请求提交参数组中
        sPara.put("sign", mysign);
        sPara.put("sign_type", "MD5");

        return sPara;
    }

}
