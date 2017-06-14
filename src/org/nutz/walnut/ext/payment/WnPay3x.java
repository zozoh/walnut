package org.nutz.walnut.ext.payment;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.util.WnRun;

/**
 * 第三方支付平台逻辑的封装接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class WnPay3x {

    // WnPayment 类会为其设置，以便子类使用
    protected WnRun run;
    protected WnIo io;

    /**
     * 在第三方平台创建订单的具体实现逻辑。
     * <p>
     * <b style="color:red">!!!注意:</b>一旦本函数会可能更新支付单对应字段，那么在返回的对象里通过
     * <code>hasChangedKeys/getChangedKeys</code>， 调用者必须要将这个更新持久化
     * 
     * @param po
     *            支付单对象
     * 
     * @param args
     *            更多发送请求时需要的参数，是不用持久化的
     * 
     * @return 支付单状态
     */
    public abstract WnPay3xRe send(WnPayObj po, String... args);

    /**
     * 查询第三方平台订单状态。
     * <p>
     * <b style="color:red">!!!注意:</b>一旦本函数会可能更新支付单对应字段，那么在返回的对象里通过
     * <code>hasChangedKeys/getChangedKeys</code>， 调用者必须要将这个更新持久化
     * 
     * @param po
     *            支付单对象
     * 
     * @return 支付单状态
     */
    public abstract WnPay3xRe check(WnPayObj po);

    /**
     * 对支付单进行后续处理
     * 
     * @param po
     *            支付单对象
     * @param req
     *            第三方平台返回的支付结果参数表
     * 
     * @return 支付单处理结果
     */
    public abstract WnPay3xRe complete(WnPayObj po, NutMap req);

}
