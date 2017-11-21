# 命令简介 

    `voucher use_coupon` 使用优惠金额

用法
=======

	voucher test_coupon -coupon_id [优惠卷id] 
	           -price [金额] 
	           -belongTo [用户名] 
	           -payId [支付单号]

    参数:
    * coupon_id 优惠卷id,必填
    * price 金额,单位为分,必填
    * belongTo 用户名, 用于强制校验优惠卷的归属,必填
    * payId 支付单号,必填

示例
=======

    # 满足条件
	voucher use_coupon -price 10002 -coupon_id r202u4ag9mgf7pcdvj5ckqs18c \
	      -belongTo wendal123 -payId NOTOK
    输出:
    {
       price: 10002,
       ok: true,
       price_new: 3
    }


    # 不满足条件
	voucher use_coupon -price 1002 -coupon_id r202u4ag9mgf7pcdvj5ckqs18c \
	      -belongTo wendal123 -payId NOTOK
    输出:
    
    {
       price: 1002,
       ok: false,
       err: "e.cmd.voucher_test_coupon.not_reach_condition",
       condition: 10000
    }