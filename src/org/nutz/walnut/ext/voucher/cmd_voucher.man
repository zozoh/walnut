# 命令简介 

    `voucher` 用来处理优惠卷相关的操作

用法
=======

```
voucher init                       # 创建本用户下的优惠卷目录
voucher create_promotion           # 创建一个促销活动
voucher create_coupon              # 创建促销活动下面的优惠卷
voucher list_coupon                # 查询(某促销活动|某人|某支付单|某时间区间|某个特定scope)内的具体优惠卷
voucher send_coupon                # 将一张未使用优惠卷送给某个用户
voucher test_coupon                # 试算优惠价
voucher use_coupon                 # 使用优惠劵,关联支付单,返回优惠之后的价格
voucher findbest_coupon            # 返回指定用户下,符合当前支付场景的最佳优惠卷(TODO)
voucher remove_coupon              # 活动创建者可以删除指定的优惠卷(TODO)
voucher remove_promotion           # 活动创建者可以删除活动(TODO)
```