package org.nutz.walnut.ext.payment;

import java.util.List;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnQuery;

/**
 * 通用的付款流程逻辑封装接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
public class WnPayment {

    /**
     * 创建一个支付单
     * 
     * @param pi
     *            支付单创建信息
     * 
     * @return 支付单对象
     */
    public WnPayObj create(WnPayInfo pi) {
        return null;
    };

    /**
     * 为支付单设置回调脚本
     * 
     * @param po
     *            支付单
     * @param cmdText
     *            回调脚本
     */
    public void setupCallbak(WnPayObj po, String cmdText) {

    };

    /**
     * 在第三方平台创建订单
     * 
     * @param payType
     *            第三方平台类型，支持
     *            <ul>
     *            <li>wx.qrcode : 微信主动扫付款码
     *            <li>wx.jsapi : 微信公众号支付
     *            <li>wx.scan : 微信被物理码枪扫码支付
     *            <li>zfb.scan : 支付宝主动扫付款码
     *            </ul>
     * @param po
     * @return 支付单处理结果
     */
    public WnPayRe send(String payType, WnPayObj po) {
        return null;
    };

    /**
     * 获取一个支付单
     * 
     * @param poId
     *            支付单 ID
     * @return 支付单对象
     */
    public WnPayObj get(String poId) {
        return null;
    };

    /**
     * 查询一组支付单
     * 
     * @param q
     *            查询条件
     * @return 支付单列表
     */
    public List<WnPayObj> query(WnQuery q) {
        return null;
    };

    /**
     * 对支付单进行后续处理
     * 
     * @param req
     *            第三方平台返回的支付结果参数表
     * @param po
     *            支付单对象
     * @return 支付单处理结果
     */
    public WnPayRe doResult(NutMap req, WnPayObj po) {
        return null;
    };

}
