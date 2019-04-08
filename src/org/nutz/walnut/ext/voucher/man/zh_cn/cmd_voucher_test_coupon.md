# 命令简介 

    `voucher test_coupon` 试算优惠金额

用法
=======

	voucher test_coupon [coupon_id] [price] [-scope traffic]

    参数:
    * coupon_id 优惠卷id,必填
    * price 金额,单位为分,必填
    * scope 优惠券要被应用的范围，选填

示例
=======

    # 满足条件
	voucher test_coupon r202u4ag9mgf7pcdvj5ckqs18c 100002 
    输出:
    {
       price: 100002,
       ok: true,
       price_new: 90003
    }

    # 不满足条件
	voucher test_coupon r202u4ag9mgf7pcdvj5ckqs18c 1002
	输出错误:
    e.cmd.voucher_test_coupon.not_reach_condition : 1002 < 10000
