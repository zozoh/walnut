package org.nutz.walnut.ext.www.impl;

import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.weixin.WnIoWeixinApi;
import org.nutz.walnut.ext.www.bean.WnOrderInfo;

public class WnOrderService {

    private WnIo io;

    private WnObj oOrderHome;
    private WnObj oProductHome;
    private WnObj oCouponHome;

    public WnOrderService(WnIo io, WnObj oOrderHome, WnObj oProductHome, WnObj oCouponHome) {
        this.io = io;
        this.oOrderHome = oOrderHome;
        this.oProductHome = oProductHome;
        this.oCouponHome = oCouponHome;
    }

    public WnObj createOrder(WnOrderInfo orInfo) {
        // 依次检查产品列表
        
        // 检查优惠券列表
        
        // 计算订单总价
        
        // 应用优惠券
        
        // 准备订单元数据
        
        // 创建订单对象
        
        // 返回创建后的订单
        return null;
    }

}
