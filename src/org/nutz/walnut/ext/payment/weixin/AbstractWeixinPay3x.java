package org.nutz.walnut.ext.payment.weixin;

import java.util.Date;

import org.nutz.http.Http;
import org.nutz.http.Response;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.Xmls;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.Mvcs;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.ext.payment.WnPay3x;
import org.nutz.walnut.ext.payment.WnPay3xDataType;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPay3xStatus;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.weixin.WxConf;
import org.nutz.weixin.util.Wxs;

public abstract class AbstractWeixinPay3x extends WnPay3x {

    protected static final String KEY_wxpay_send = "wxpay_send";
    protected static final String KEY_wxpay_result = "wxpay_result";
    protected static final String KEY_wxpay_st = "wxpay_st";

    /**
     * 调用微信的统一下单接口
     * 
     * @param po
     *            支付单对象
     * 
     * @param openid
     *            公众号支付时的 openID，null 为默认
     * 
     * @return 支付结果描述对象
     */
    protected WnPay3xRe unifiedorder(WnPayObj po, String openid) {
        // 准备返回对象
        WnPay3xRe re = new WnPay3xRe();

        // 读取微信配置信息
        WxConf conf = this.getConfig(po);

        // 准备订单标题
        String brief = po.getString(WnPayObj.KEY_BRIEF, "测试商品");

        // 准备一个 Map
        NutMap map = new NutMap();
        map.setv("body", brief);
        map.setv("out_trade_no", po.id());
        map.setv("total_fee", po.getInt(WnPayObj.KEY_FEE));

        // 填充用户 openid
        if (null != openid)
            map.setv("openid", openid);

        // 填充客户端 IP
        this._fill_client_ip(map);

        // 过期时间（增加 1 分钟作为通讯补偿）
        long pay_expired = Math.max(conf.pay_time_expire, 10) * 60 * 1000 + 60000;
        Date d = Times.D(System.currentTimeMillis() + pay_expired);
        String ds = Times.format("yyyyMMddHHmmss", d);
        map.setv("time_expire", ds);

        // 填充这个 Map 其他字段
        map.setv("appid", conf.appID);
        map.setv("mch_id", conf.pay_mch_id);
        map.setv("device_info", "WEB");
        map.setv("notify_url", conf.pay_notify_url);
        map.setv("trade_type", "NATIVE");

        // 对 Map 进行签名
        Wxs.fillPayMap(map, conf.pay_key);

        // 转换 HTML
        String reqXML = Xmls.mapToXml(map);

        // 记录发送的数据
        po.setv(KEY_wxpay_send, reqXML);
        re.addChangeKeys(KEY_wxpay_send);

        // 发送到统一下单接口
        Response resp = Http.postXML("https://api.mch.weixin.qq.com/pay/unifiedorder",
                                     reqXML,
                                     15 * 1000);
        String respXml = resp.getContent();

        // 记录收到的数据
        po.setv(KEY_wxpay_result, respXml);
        re.addChangeKeys(KEY_wxpay_result);

        // 验证返回的 XML
        NutMap respMap = Wxs.checkPayReturn(respXml, conf.pay_key);

        // 得到返回对象的状态
        Object returnCode = respMap.get("return_code");
        Object resultCode = respMap.get("result_code");

        // 交易成功
        if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
            re.setStatus(WnPay3xStatus.WAIT);
            // 二维码
            String code_url = respMap.getString("code_url");
            if (!Strings.isBlank(code_url)) {
                re.setDataType(WnPay3xDataType.QRCODE);
                re.setData(code_url);
            }
            // 其他的
            else {
                re.setDataType(WnPay3xDataType.JSON);
                re.setData(respMap);
            }
        }
        // 交易失败
        else {
            re.setStatus(WnPay3xStatus.FAIL);
            re.setDataType(WnPay3xDataType.JSON);
            re.setData(respMap);
        }

        // 返回结果
        return re;
    }

    protected WnPay3xRe notify_result(WnPayObj po, NutMap req) {
        // 准备返回结果
        WnPay3xRe re = new WnPay3xRe();

        // 读取微信配置信息
        WxConf conf = this.getConfig(po);

        // 检查签名
        NutMap respMap = Wxs.checkPayReturnMap(req, conf.pay_key);

        // 得到返回对象的状态
        Object returnCode = respMap.get("return_code");
        Object resultCode = respMap.get("result_code");

        // 填充返回值类型
        re.setDataType(WnPay3xDataType.JSON);
        re.setData(respMap);

        // 交易成功
        if ("SUCCESS".equals(returnCode) && "SUCCESS".equals(resultCode)) {
            re.setStatus(WnPay3xStatus.OK);
        }
        // 交易失败
        else {
            re.setStatus(WnPay3xStatus.FAIL);
        }

        // 返回结果
        return re;
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
        NutMap result = Wxs.checkPayReturn(tmp, conf.pay_key);

        return __handle_result_for_check(conf, po, result);
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

    protected WxConf getConfig(WnPayObj po) {
        WnObj oConf = __get_weixin_conf_obj(po);
        return io.readJson(oConf, WxConf.class);
    }

    protected WnIoWeixinApi getWeixinApi(WnPayObj po) {
        WnObj oConf = __get_weixin_conf_obj(po);
        return new WnIoWeixinApi(run.io(), oConf);
    }

    protected void _fill_client_ip(NutMap params) {
        if (Mvcs.getReq() == null)
            params.put("spbill_create_ip", "8.8.8.8");
        else
            params.put("spbill_create_ip", Lang.getIP(Mvcs.getReq()));
    }

    private WnObj __get_weixin_conf_obj(WnPayObj po) {
        WnUsr seller = run.usrs().check("id:" + po.getString(WnPayObj.KEY_SELLER_ID));
        WnObj oConf = io.check(null, seller.home() + "/.weixin/" + seller.name() + "/wxconf");
        return oConf;
    }

}
