# 命令简介 

    `voucher create_promotion` 创建一个优惠活动

用法
=======

	voucher create_promotion -name [短英文名] 
	               -title [活动的描述] 
	               -totalNum [优惠卷总数量] 
	               -startTime [开始时间戳] 
	               -endTime [结束时间戳] 
	               -condition [最低生效金额] 
	               -discount [满减金额或折扣]

    参数:
    * name 短英文名,必须唯一,必填
    * title 中文描述,可以很长,必填
    * totalNum 优惠卷的总数量,必填
    * startTime 开始时间,必填
    * endTime 结束时间,必填
    * condition 最低生效金额,单位是分,可选
    * discount 满减金额(大于1) 或者 折扣(小于1)

示例
=======

    # 创建一个优惠活动
	voucher create_promotion -name "testC" -title "测试优惠卷C" -totalNum 1 -startTime 0 -endTime 170000000000 -condition 10000 -discount 9999
    输出:   
    ```
    [{
       id: "r202u4ag9mgf7pcdvj5ckqs18c",
       race: "FILE",
       ct: 1509601353442,
       lm: 1509601353448,
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
       voucher_payId: ""
    }]
    ```

注意: 生成活动后,不会马上生成优惠卷.

重复执行`voucher create_promotion`可以修改已有的优惠活动,前提是优惠卷尚未生成.