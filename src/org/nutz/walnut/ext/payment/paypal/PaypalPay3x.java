package org.nutz.walnut.ext.payment.paypal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.payment.WnPay3x;
import org.nutz.walnut.ext.payment.WnPay3xDataType;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPay3xStatus;
import org.nutz.walnut.ext.payment.WnPayObj;

import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.http.serializer.ObjectMapper;
import com.paypal.orders.AmountWithBreakdown;
import com.paypal.orders.ApplicationContext;
import com.paypal.orders.Order;
import com.paypal.orders.OrderRequest;
import com.paypal.orders.OrdersCaptureRequest;
import com.paypal.orders.OrdersCreateRequest;
import com.paypal.orders.OrdersGetRequest;
import com.paypal.orders.PurchaseUnitRequest;

/**
 * PayPal账单有多个状态 CREATE: 新建订单 APPROVED: 客户已提交付款信息,等待服务器做二次确认.
 * 本实现中,如果是这个状态,会发起CAPTURE命令,自动确认订单. COMPLETED: 完成支付,完成确认
 */
public class PaypalPay3x extends WnPay3x {

    private static final Log log = Logs.get();

    public static String PP_STAT = "paypal_stat"; // Paypal支付单状态
    public static String PP_RE = "paypal_re"; // 最后查询的状态
    public static String PP_ID = "paypal_id"; // Paypal支付单的id

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        WnPay3xRe re = new WnPay3xRe();
        re.setStatus(WnPay3xStatus.WAIT);
        re.setDataType(WnPay3xDataType.JSON);

        PaypalConfig conf = createConfig(po);
        PayPalHttpClient client = createClient(conf);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");
        
		// 设置回调地址
		ApplicationContext app = new ApplicationContext();
		if (!Strings.isBlank(conf.cancelUrl)) {
			app.cancelUrl(conf.cancelUrl);
		}
		if (!Strings.isBlank(conf.returnUrl)) {
			app.returnUrl(conf.returnUrl);
		}
		orderRequest.applicationContext(app);
        
        List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
        AmountWithBreakdown abd = new AmountWithBreakdown();
        abd.currencyCode(Strings.sBlank(po.getCurrency(), "USD"));
        abd.value(String.format("%.2f", po.getFeeInYuan()));
        PurchaseUnitRequest pur = new PurchaseUnitRequest();
        pur.amountWithBreakdown(abd);
        pur.description(po.getBrief("tWalnt"));
        pur.referenceId(po.id());
        purchaseUnits.add(pur);
        orderRequest.purchaseUnits(purchaseUnits);
        OrdersCreateRequest request = new OrdersCreateRequest().requestBody(orderRequest);
        try {
            // Call API with your client and get a response for your call
            HttpResponse<Order> response = client.execute(request);

            // If call returns body in response, you can get the de-serialized
            // version by
            // calling result() on the response
            Order order = response.result();
            Map<String, Object> ret = ObjectMapper.map(order);
            po.put(PP_ID, order.id());
            po.put(PP_RE, ret);
            po.put(PP_STAT, order.status());
            re.addChangeKeys(PP_ID, PP_RE, PP_STAT);
            re.setData(ret);
        }
        catch (Exception ioe) {
            log.info("paypal fail", ioe);
        }
        return re;
    }

    @Override
    public WnPay3xRe check(WnPayObj po) {
        WnPay3xRe re = new WnPay3xRe();
        re.setDataType(WnPay3xDataType.JSON);
        re.setStatus(WnPay3xStatus.WAIT);

        PaypalConfig conf = createConfig(po);
        PayPalHttpClient client = createClient(conf);

        Order order = null;
        OrdersGetRequest request = new OrdersGetRequest(po.getString(PP_ID));
        try {
            // Call API with your client and get a response for your call
            HttpResponse<Order> response = client.execute(request);

            // If call returns body in response, you can get the de-serialized
            // version by
            // calling result() on the response
            order = response.result();
            // 如果状态是
            if ("COMPLETED".equals(order.status())) {
                re.setStatus(WnPay3xStatus.OK);
            } else if ("APPROVED".equals(order.status())) {
                log.info("paypal order APPROVED, send Capture");
                OrdersCaptureRequest req2 = new OrdersCaptureRequest(order.id());
                HttpResponse<Order> resp2 = client.execute(req2);
                order = resp2.result();
                if ("COMPLETED".equals(order.status())) {
                    re.setStatus(WnPay3xStatus.OK);
                }
            }
            Map<String, Object> ret = ObjectMapper.map(order);
            po.put(PP_RE, ret);
            po.put(PP_STAT, order.status());
            re.addChangeKeys(PP_RE, PP_STAT); // 不要更新PP_ID
            re.setData(ret);
        }
        catch (Exception ioe) {
            log.info("paypal fail", ioe);
        }

        return re;
    }

    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        return check(po); // 直接与paypal通信,获取最新状态
    }

    protected PaypalConfig createConfig(WnPayObj po) {
        WnAccount seller = run.auth().checkAccount(po.getSellerId());
        String aph = seller.getHomePath() + "/.paypal/" + po.getPayTarget() + "/ppconf";
        WnObj conf = io.check(null, aph);
        return io.readJson(conf, PaypalConfig.class);
    }

    protected PayPalHttpClient createClient(PaypalConfig pc) {
        PayPalEnvironment env;
        if ("sanbox".equals(pc.mode)) {
            env = new PayPalEnvironment.Sandbox(pc.id, pc.secret);
        } else {
            env = new PayPalEnvironment.Live(pc.id, pc.secret);
        }
        PayPalHttpClient client = new PayPalHttpClient(env);
        return client;
    }
}
