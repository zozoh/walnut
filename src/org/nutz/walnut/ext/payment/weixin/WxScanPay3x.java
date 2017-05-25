package org.nutz.walnut.ext.payment.weixin;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.lang.Lang;
import org.nutz.lang.Xmls;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.ext.payment.WnPay3x;
import org.nutz.walnut.ext.payment.WnPay3xDataType;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPay3xStatus;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.weixin.WxConf;
import org.nutz.weixin.util.Wxs;

/**
 * 微信被物理码枪扫付款码支付
 * <p/>
 * 刷卡支付下单API
 * https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=9_10&index=1
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WxScanPay3x extends WnPay3x {

    private static final Log log = Logs.get();

    private static final String KEY_wxpay_send = "wxpay_send";
    private static final String KEY_wxpay_result = "wxpay_result";
    private static final String KEY_wxpay_st = "wxpay_st";

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        WnPay3xRe re = new WnPay3xRe();
        re.setDataType(WnPay3xDataType.JSON);
        WxConf conf = getConfig(po);

        String dst = "https://api.mch.weixin.qq.com/pay/micropay";
        String auth_code = args[0].trim(); // 授权码. 即扫码枪读取到的18位数字
        if (!auth_code.matches("^(10|11|12|13|14|15)[0-9]{16}$")) {
            log.info("非法的微信支付授权码 " + auth_code);
            throw Lang.impossible();
        }

        int total_fee = po.getInt(WnPayObj.KEY_FEE);

        // zozoh : 实现类不需要考虑缓存问题吧，铁定发就对了
        // NutMap params = po.getAs(KEY_wxpay_send, NutMap.class);
        // if (params == null) {
        NutMap params = new NutMap();
        params.put("appid", conf.appID);
        params.put("mch_id", conf.pay_mch_id);
        params.put("nonce_str", R.UU32());
        params.put("body", po.getString(WnPayObj.KEY_BRIEF));
        params.put("attach", po.id());
        params.put("out_trade_no", po.id());
        params.put("total_fee", total_fee);
        if (Mvcs.getReq() == null)
            params.put("spbill_create_ip", "8.8.8.8");
        else
            params.put("spbill_create_ip", Lang.getIP(Mvcs.getReq()));
        params.put("auth_code", auth_code);
        Wxs.fillPayMap(params, conf.pay_key);

        po.setv(KEY_wxpay_send, params);
        re.addChangeKeys(KEY_wxpay_send);
        // }

        String reqXML = Xmls.mapToXml(params);

        log.debug("reqXML = " + reqXML);

        Response resp = Http.postXML(dst, reqXML, 15 * 1000);
        String tmp = resp.getContent();
        NutMap result = Xmls.xmlToMap(tmp);

        return __handle_result_for_send(re, result);
    }

    @Override
    public WnPay3xRe check(WnPayObj po) {
        WxConf conf = getConfig(po);
        // https://api.mch.weixin.qq.com/pay/orderquery
        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        NutMap params = new NutMap();
        params.setv("appid", conf.appID);
        params.setv("mch_id", conf.pay_mch_id);
        params.setv("out_trade_no", po.id());
        params.setv("nonce_str", R.UU32());
        Wxs.fillPayMap(params, conf.pay_key);

        Response resp = Http.postXML(url, Xmls.mapToXml(params), 15 * 1000);
        String tmp = resp.getContent();
        NutMap result = Xmls.xmlToMap(tmp);
        // TODO 需要验证一下 sign

        return __handle_result_for_check(conf, po, result);
    }

    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        return check(po);
    }

    private WnPay3xRe __handle_result_for_check(WxConf conf, WnPayObj po, NutMap result) {
        WnPay3xRe re = new WnPay3xRe();
        re.setDataType(WnPay3xDataType.JSON);
        re.addChangeKeys(KEY_wxpay_result);
        po.put(KEY_wxpay_result, result);
        // 通信状态,不是交易状态!!!
        // 如果通信状态不是成功,打印原因,并返回!!
        if (result.is("return_code", "SUCCESS") && result.is("result_code", "SUCCESS")) {
            String ts = result.getString("trade_state");
            // 等待
            if ("USERPAYING".equals(ts)) {
                // 超时的话，就撤销订单，并标识
                long ms = System.currentTimeMillis() - po.getLong("send_at", 0);
                if (ms > 60000L) {
                    // String url =
                    // "https://api.mch.weixin.qq.com/secapi/pay/reverse";
                    // NutMap params = new NutMap();
                    // params.setv("appid", conf.appID);
                    // params.setv("mch_id", conf.pay_mch_id);
                    // params.setv("out_trade_no", po.id());
                    // params.setv("nonce_str", R.UU32());
                    // Wxs.fillPayMap(params, conf.pay_key);
                    //
                    // Response resp = Http.postXML(url, Xmls.mapToXml(params),
                    // 15 * 1000);
                    // String tmp = resp.getContent();
                    // NutMap rere = Xmls.xmlToMap(tmp);
                    // // TODO 需要验证一下 sign
                    //
                    // // 撤销成功，改成 FAIL
                    // if (rere.is("result_code ", "SUCCESS")) {
                    // re.setStatus(WnPay3xStatus.FAIL);
                    // }
                    // // 撤销失败，维持 WAIT 状态
                    // else {
                    // re.setStatus(WnPay3xStatus.WAIT);
                    // }
                    re.setStatus(WnPay3xStatus.FAIL);
                    re.addChangeKeys(KEY_wxpay_st);
                    po.setv(KEY_wxpay_st, "_TIMEOUT");
                }
                // 否则标识
                else {
                    re.setStatus(WnPay3xStatus.WAIT);
                }
            }
            // 成功
            else if ("SUCCESS".equals(ts)) {
                re.setStatus(WnPay3xStatus.OK);
            }
            // 其他统统算失败先
            else {
                re.setStatus(WnPay3xStatus.FAIL);
            }

            // 交易成功!
            re.setData(result);
            return re;
        }
        // 失败
        re.setStatus(WnPay3xStatus.FAIL);
        re.setData(result);
        return re;
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

    public WxConf getConfig(WnPayObj po) {
        WnUsr seller = run.usrs().check("id:" + po.getString(WnPayObj.KEY_SELLER_ID));
        WnObj conf = io.check(null, seller.home() + "/.weixin/" + seller.name() + "/wxconf");
        return io.readJson(conf, WxConf.class);
    }
}
