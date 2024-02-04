package org.nutz.walnut.ext.net.payment.weixin;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.lang.Xmls;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.net.payment.WnPay3xDataType;
import org.nutz.walnut.ext.net.payment.WnPay3xRe;
import org.nutz.walnut.ext.net.payment.WnPay3xStatus;
import org.nutz.walnut.ext.net.payment.WnPayObj;
import org.nutz.walnut.ext.net.weixin.WxConf;
import org.nutz.weixin.util.Wxs;

/**
 * 微信被物理码枪扫付款码支付
 * <p/>
 * 刷卡支付下单API
 * https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=9_10&index=1
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WxScanPay3x extends AbstractWeixinPay3x {

    //private static final Log log = Wlog.getCMD();

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        WnPay3xRe re = new WnPay3xRe();
        re.setDataType(WnPay3xDataType.JSON);
        WxConf conf = getConfig(po);

        String dst = "https://api.mch.weixin.qq.com/pay/micropay";
        String auth_code = args[0].trim(); // 授权码. 即扫码枪读取到的18位数字
        if (!auth_code.matches("^(10|11|12|13|14|15)[0-9]{16}$")) {
            // log.info("非法的微信支付授权码 " + auth_code);
            throw Er.create("e.pay.send.invalid.payCode", auth_code);
        }

        int total_fee = po.getFee();

        // zozoh : 实现类不需要考虑缓存问题吧，铁定发就对了
        // NutMap params = po.getAs(KEY_wxpay_send, NutMap.class);
        // if (params == null) {
        NutMap params = new NutMap();
        params.put("appid", conf.appID);
        params.put("mch_id", conf.pay_mch_id);
        params.put("nonce_str", R.UU32());
        params.put("body", po.getBrief("测试商品"));
        params.put("out_trade_no", po.id());
        params.put("total_fee", total_fee);
        _fill_client_ip(params);
        params.put("auth_code", auth_code);
        Wxs.fillPayMap(params, conf.pay_key);

        po.setv(KEY_wxpay_send, params);
        re.addChangeKeys(KEY_wxpay_send);
        // }

        String reqXML = Xmls.mapToXml(params);

        //log.debug("reqXML = " + reqXML);

        Response resp = Http.postXML(dst, reqXML, 15 * 1000);
        String tmp = resp.getContent();
        NutMap result = Xmls.xmlToMap(tmp);

        return __handle_result_for_send(re, result);
    }

    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        return check(po);
    }

    private WnPay3xRe __handle_result_for_send(WnPay3xRe re, NutMap result) {
        re.setDataType(WnPay3xDataType.JSON);
        // 通信状态,不是交易状态!!!
        // 如果通信状态不是成功,打印原因,并返回!!
        if (!"SUCCESS".equals(result.get("return_code"))) {
            re.setStatus(WnPay3xStatus.FAIL);
            re.setData(result);
            return re;
        }
        // 交易状态
        if (!"SUCCESS".equals(result.get("result_code"))) {
            if ("USERPAYING".equals(result.get("err_code"))) {
                // 需要用户确认
                re.setStatus(WnPay3xStatus.WAIT);
            } else {
                re.setStatus(WnPay3xStatus.FAIL);
            }
            re.setData(result);
            return re;
        }

        // 交易成功!
        re.setStatus(WnPay3xStatus.OK);
        re.setDataType(WnPay3xDataType.JSON);
        re.setData(result);
        return re;
    }

}
