package org.nutz.walnut.ext.payment.weixin;

import org.nutz.http.Request;
import org.nutz.http.Request.METHOD;
import org.nutz.http.Response;
import org.nutz.http.Sender;
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
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.weixin.WxConf;

/**
 * 微信被物理码枪扫付款码支付
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WxScanPay3x extends WnPay3x {
    
    private static final Log log = Logs.get();

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        WxConf conf = getConfig(po);
        // 刷卡支付下单API https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=9_10&index=1
        String dst = "https://api.mch.weixin.qq.com/pay/micropay";
        String auth_code = args[0].trim(); //授权码. 即扫码枪读取到的18位数字
        if (!auth_code.matches("^(10|11|12|13|14|15)[0-9]{16}$")) {
            log.info("非法的微信支付授权码 " + auth_code);
            throw Lang.impossible();
        }
        NutMap params = new NutMap();
        
        params.put("appid", conf.appID);
        params.put("mch_id", conf.pay_mch_id);
        params.put("nonce_str", R.UU32());
        params.put("body", po.getString(WnPayObj.KEY_BRIEF));
        params.put("attach", po.id());
        params.put("out_trade_no", po.get("out_trade_no", R.UU32()));
        params.put("total_fee", (int)(po.getDouble(WnPayObj.KEY_FEE) * 100));
        if (Mvcs.getReq() == null)
            params.put("spbill_create_ip", "8.8.8.8");
        else
            params.put("spbill_create_ip", Lang.getIP(Mvcs.getReq()));
        params.put("auth_code", auth_code);
        
        
        String reqXML = Xmls.mapToXml(params);
        
        log.debug("reqXML = " + reqXML);
        
        Request req = Request.create(dst, METHOD.POST);
        req.setData(reqXML);
        Response resp = Sender.create(req).send();
        String tmp = resp.getContent();
        NutMap result = Xmls.xmlToMap(tmp);
        
        WnPay3xRe re = new WnPay3xRe();
        re.setDataType(WnPay3xDataType.JSON);
        re.setData(result);
        return re;
    }

    @Override
    public WnPay3xRe check(WnPayObj po) {
        throw Lang.noImplement();
    }

    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        throw Lang.noImplement();
    }

    public WxConf getConfig(WnPayObj po) {
        WnUsr seller = run.usrs().check("id:" + po.getString(WnPayObj.KEY_SELLER_ID));
        WnObj conf = io.check(null, seller.home() + "/.payment/conf/wxpay");
        return io.readJson(conf, WxConf.class);
    }
}
