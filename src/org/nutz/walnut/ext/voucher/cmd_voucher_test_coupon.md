# 命令简介 

    `voucher test_coupon` 试算优惠金额

用法
=======

	voucher test_coupon -coupon_id [优惠卷id] -price [金额]

    参数:
    * coupon_id 优惠卷id,必填
    * price 金额,单位为分,必填

示例
=======

    # 满足条件
	voucher test_coupon -price 100002 -coupon_id r202u4ag9mgf7pcdvj5ckqs18c
    输出:
    {
       price: 100002,
       ok: true,
       price_new: 90003
    }

    # 不满足条件
	voucher test_coupon -price 1002 -coupon_id r202u4ag9mgf7pcdvj5ckqs18c
	输出:
    {
       price: 1002,
       ok: false,
       err: "e.cmd.voucher_test_coupon.not_reach_condition",
       condition: 10000
    }