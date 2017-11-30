# 命令简介 

    `voucher use_coupon` 使用优惠金额

用法
=======

	voucher use_coupon [couponId] [price] [belongTo]
	       [-payId 支付单号] [-scope traffic]

    参数:
    * couponId 优惠卷id,必填
    * price 金额,单位为分,必填
    * belongTo 用户名, 用于强制校验优惠卷的归属,必填
    * payId 支付单号,必填
    * scope 优惠券要被应用的范围，选填

示例
=======

    # 满足条件
	voucher use_coupon r202u4ag9mgf7pcdvj5ckqs18c 10002 wendal123 -payId NOTOK
    输出:
    {
        .. 这里是 Coupon 对象的 JSON ..
    }


    # 不满足条件
	voucher use_coupon r202u4ag9mgf7pcdvj5ckqs18c -price 1002 wendal123 -payId NOTOK
    输出错误:
    e.cmd.voucher_test_coupon.not_reach_condition : 1002 < 10000