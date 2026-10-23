# 过滤器简介

`@query` 查询一组支付单状态。

# 用法

```bash
pay @query [简介关键字] [-bu 过滤项] [-se 过滤项] [-fee 区间] [-cur 货币] [-c_at 时间] [-s_at 时间] [-st 状态] [-match JSON] [-e 正则] [-t 字段] [-bishcqnl] [-ibase N] [-pager] [-limit N] [-skip N] [-json]
  # 简介关键字    # brief 段的过滤关键字
  -bu        # 买家过滤项：类型:ID:名称，类型 @wn 表示 Walnut 用户
             #  "@wn::" 全部 walnut 用户 | ":45..d2:" 指定 buyer_id | "::xiaobai" 指定 buyer_nm
  -se        # 服务商过滤项：ID:名称（如 ":redfox"）
             # 开启本项需要 op/root 成员权限，否则强制以当前域为约束
  -fee       # 金额过滤项，浮数区间，如 [100,200]
  -cur       # 货币单位过滤项，会强制大写
  -c_at      # 完成时间过滤项；0 表示所有未完成的支付单；支持区间及 %ms 宏
  -s_at      # 发送请求时间过滤项；0 表示所有未发送的支付单；支持区间及 %ms 宏
  -st        # 支付单状态过滤项，支持正则表达式
  -match     # 更多对象查询条件 JSON
  -e -t -bishcqnl -ibase -pager -limit -skip -json    # 与 o @query 等效的输出控制参数，limit 默认 10
```

# 示例

```bash
# 查询指定金额范围的已完成支付单
pay @query -fee [100,200] -st '^(OK|FAIL)'

# 按关闭时间查询已完成的支付单
pay @query -fee [100,200] -c_at [1,]

# 查询某买家等待 1 小时还未确认的支付单
pay @query -bu ::xiaobai -c_at '[,%ms:now-1h]' -st WAIT
```
