package org.nutz.walnut.ext.payment;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;

/**
 * 第三方支付平台逻辑的封装接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnPay3X {

    /**
     * 在第三方平台创建订单的具体实现逻辑
     * 
     * @param io
     *            IO 接口
     * @param po
     *            支付单对象
     * 
     * @return 支付单处理结果
     */
    WnPayRe send(WnIo io, WnPayObj po);

    /**
     * 对支付单进行后续处理
     * 
     * @param io
     *            IO 接口
     * @param req
     *            第三方平台返回的支付结果参数表
     * @param po
     *            支付单对象
     * @return 支付单处理结果
     */
    WnPayRe doResult(WnIo io, NutMap req, WnPayObj po);

}
