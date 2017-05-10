package org.nutz.walnut.ext.payment;

import org.nutz.lang.util.NutMap;

/**
 * 第三方支付平台逻辑的封装接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnPay3x {

    /**
     * 在第三方平台创建订单的具体实现逻辑。
     * <p>
     * <b style="color:red">!!!注意:</b>一旦本函数会可能更新支付单对应字段，那么在返回的对象里通过
     * <code>hasChangedKeys/getChangedKeys</code>， 调用者必须要将这个更新持久化
     * 
     * @param po
     *            支付单对象
     * 
     * @return 支付单状态
     */
    WnPay3xRe send(WnPayObj po);

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
    WnPay3xRe check(WnPayObj po);

    /**
     * 对支付单进行后续处理
     * @param po
     *            支付单对象
     * @param req
     *            第三方平台返回的支付结果参数表
     * 
     * @return 支付单处理结果
     */
    WnPay3xRe complete(WnPayObj po, NutMap req);

}
