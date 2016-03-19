package org.nutz.walnut.ext.weixin.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.Xmls;
import org.nutz.lang.random.R;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.ajax.Ajax;
import org.nutz.weixin.util.Wxs;

/**
 * 命令的使用方法:
 * 
 * <pre>
 * # 生成 JS-SDK 的支付准备对象
 * weixin xxx order -submit xxx -openid xxx -client_ip xxx
 * </pre>
 * 
 * 参数说明:
 * <ul>
 * <li><b>openid</b> : 由哪个用户发起的请求！必须
 * <li><b>client_ip</b> : 请求者的 IP
 * <li><b>brief</b> : 订单的简要说明，支持 Nutz Tmpl 模板，上下文就是订单对象本身。默认为 "WeixinOrder"
 * <li><b>openid_key</b> : 订单里，哪个字段为 openid，默认 "openid"
 * <li><b>debug</b> : 仅仅是调试用，在 -submit 模式下，并不发送数据
 * <li><b>ajax</b> : 将输出的内容用 AJAX 包裹
 * </ul>
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@JvmHdlParamArgs("^(debug|c|n|q|ajax)$")
public class weixin_order implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        if (null == hc.oHome)
            throw Er.create("e.cmd.weixin.pnb.blank");

        // 准备返回值
        Object re;

        // 提交订单
        if (hc.params.has("submit")) {
            NutMap map = this.__do_submit(sys, hc);
            if (hc.params.is("ajax")) {
                re = Ajax.ok().setData(map);
            } else {
                re = map;
            }
        }
        // 接受订单完成的回调
        else if (hc.params.has("result")) {
            // 成功
            if (this.__do_result(sys, hc)) {
                re = hc.params.is("ajax") ? Ajax.ok() : true;
            }
            // 失败
            else {
                re = hc.params.is("ajax") ? Ajax.fail() : false;
            }
        }
        // 靠
        else {
            throw Er.create("e.cmd.weixin.order.unknownMode");
        }

        // 打印结果
        sys.out.println(Json.toJson(re, hc.jfmt));
    }

    private boolean __do_result(WnSystem sys, JvmHdlContext hc) {
        // 公众号ID
        String pnb = hc.oHome.name();

        // 分析参数
        String reid = hc.params.check("result");
        String cmdText = String.format("weixin -pnb %s -payre %s", pnb, reid);

        String json = sys.exec2(cmdText);
        NutMap map = Json.fromJson(NutMap.class, json);

        // 如果付款成功
        if ("SUCCESS".equals(map.getString("return_code"))
            && "SUCCESS".equals(map.getString("result_code"))) {

            // 获取对应账单
            String orderId = this.__get_order_id_by_out_trade_no(map.getString("out_trade_no"));
            WnObj oOrder = sys.io.checkById(orderId);

            // 如果订单已经被处理完毕了，就没必要后续处理了
            // 因为微信支付会反复调用这个回调好几遍的
            if (oOrder.getInt("or_status") > 0) {
                return true;
            }

            // 修改账单状态
            oOrder.setv("pay_result", map);
            oOrder.setv("pay_time", System.currentTimeMillis());
            oOrder.setv("or_status", 1);
            sys.io.set(oOrder, "^(pay_result|pay_time|or_status)$");

            // 返回 true 表示成功，噢耶
            return true;
        }

        // 否则返回失败
        return false;
    }

    private NutMap __do_submit(WnSystem sys, JvmHdlContext hc) {
        // 公众号ID
        String pnb = hc.oHome.name();

        // 分析参数
        String orderId = hc.params.check("submit");
        String clientIp = hc.params.check("client_ip");

        // 首先找到订单
        WnObj oOrder = sys.io.checkById(orderId);

        // 得到 OpenID 的字段
        String openIdKey = hc.params.get("openid_key", "openid");

        // 得到到患者的名称和 openId
        String or_openid = oOrder.getString(openIdKey);

        // !!!
        // 防守一下
        String client_openid = hc.params.check("openid");
        if (!client_openid.equals(or_openid)) {
            throw Er.create("e.cmd.weixin.order.user.nosame");
        }

        // 看看是否有缓存对象
        NutMap mapPay = oOrder.getAs("pay_jsobj", NutMap.class);
        if (null != mapPay) {
            return mapPay;
        }

        // 看看以前有木有提交过
        String xml = oOrder.getString("pay_rexml");

        // 如果没有提交过，那么去微信支付创建订单
        if (Strings.isBlank(xml)) {
            // 准备订单标题
            String briefT = hc.params.get("brief", "WeixinOrder");
            String brief = Tmpl.exec(briefT, oOrder);

            // 准备一个 Map
            NutMap map = new NutMap();
            map.setv("body", brief);
            map.setv("out_trade_no", this.__gen_out_trade_no(sys, oOrder));
            map.setv("total_fee", oOrder.getInt("pay_val"));
            map.setv("openid", or_openid);
            map.setv("spbill_create_ip", clientIp);

            // 设置订单过期时间
            long order_expi = oOrder.getLong("order_expi", -1);
            if (order_expi > 0)
                map.setv("time_expire", Times.format("yyyyMMddHHmmss", Times.D(order_expi)));

            // 对 Map 进行签名以及填充
            String json = Json.toJson(map);
            String cmdText = String.format("weixin %s pay", pnb);
            String xmlSigned = sys.exec2(cmdText, json);

            NutMap mapSend = Xmls.xmlToMap(xmlSigned);

            // 调试模式，并不会真的发送，只是输出签名的 Map 就好了
            if (hc.params.is("debug")) {
                return mapSend;
            }

            // 这个签好名的 map 将会被发送，先保存到订单记录里
            oOrder.setv("pay_type", "weixin");
            oOrder.setv("pay_send", mapSend);
            sys.io.set(oOrder, "^(pay_type|pay_send)$");

            // 发送请求
            cmdText = "httpc POST https://api.mch.weixin.qq.com/pay/unifiedorder";
            xml = sys.exec2(cmdText, xmlSigned);

            oOrder.setv("pay_rexml", xml);
            sys.io.set(oOrder, "^(pay_rexml)$");
        }

        // 得到公众号的签名秘钥，以及 appId 以备后用
        String wxinfo = sys.exec2("weixin " + pnb + " -info '^(appID|payKey)$'");
        NutMap mapInfo = Json.fromJson(NutMap.class, wxinfo);
        String appId = mapInfo.getString("appID");
        String payKey = mapInfo.getString("payKey");

        // 验证返回
        NutMap mapResp = Wxs.checkPayReturn(xml, payKey);

        // 记录到订单对象
        oOrder.setv("pay_remap", mapResp);
        sys.io.set(oOrder, "^(pay_remap)$");

        // 然后生成一个预付款的对象，输出到标准输出
        // 这个输出是一段 JSON，主要是给浏览器的 JSSDK 准备微信支付调用用的
        String prepay_id = mapResp.getString("prepay_id");
        mapPay = new NutMap();
        mapPay.setv("timestamp", System.currentTimeMillis() + "");
        mapPay.setv("nonceStr", R.random(100000000, 1000000000) + "");
        mapPay.setv("package", "prepay_id=" + prepay_id);
        mapPay.setv("signType", "MD5");
        mapPay.setv("appId", appId);

        String sign = Wxs.genPaySignMD5(mapPay, payKey);
        mapPay.remove("appId");
        mapPay.setv("paySign", sign);

        // 缓存到订单对象
        oOrder.setv("pay_jsobj", mapPay);
        sys.io.set(oOrder, "^(pay_jsobj)$");

        // 返回给输出者吧
        return mapPay;
    }

    private String __gen_out_trade_no(WnSystem sys, WnObj oOrder) {
        int seq = sys.io.inc(oOrder.id(), "or_pay_seq", 1);
        return oOrder.id() + "_" + seq;
    }

    private String __get_order_id_by_out_trade_no(String otn) {
        int pos = otn.lastIndexOf('_');
        if (pos > 0)
            return otn.substring(0, pos);
        return otn;
    }

}
