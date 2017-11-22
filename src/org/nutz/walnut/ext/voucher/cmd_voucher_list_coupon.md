# 命令简介 

    `voucher list_coupon` 查询(某促销活动|某人|某支付单|某时间区间|某个特定scope)内的具体优惠卷

用法
=======

	voucher list_coupon -name [短英文名] 
	           -belongTo [用户名] 
	           -startTime [开始时间] 
	           -endTime [结束时间] 
	           -payId [支付单号]

    参数:
    * name 促销活动名称,必填
    * belongTo 用户名
    * startTime 生效时间
    * endTime 结束时间
    * payId 支付单号, 传空字符串,可以得到可用优惠卷, 也传具体支付单号

示例
=======
    
    # 查询某指定优惠活动某用户的代金券 
    voucher list_coupon -name testC -payId "" -belongTo wendal123
    输出:
    ```
    [{
       id: "r202u4ag9mgf7pcdvj5ckqs18c",
       race: "FILE",
       ct: 1509601353442,
       lm: 1509601528413,
       nm: "7vl2t4o376hplrvd7376i67524",
       tp: "txt",
       mime: "text/plain",
       pid: "5unrvr1p0og2nquf5mm54s3k1k",
       d0: "sys",
       d1: "voucher",
       c: "wendal",
       m: "wendal",
       g: "wendal",
       md: 488,
       voucher_title: "测试优惠卷C",
       voucher_totalNum: 1,
       voucher_startTime: 0,
       voucher_endTime: 170000000000,
       voucher_scope: [],
       voucher_condition: 10000,
       voucher_discount: 9999.0,
       voucher_payId: "",
       voucher_belongTo: "wendal123"
    }]
    ```