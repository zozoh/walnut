# 过滤器简介

`@create` 创建一个支付单。

# 用法

```bash
pay @create -bu 买家 [-fee 金额] [-cur 货币] [-br 简介] [-se 卖家域] [-meta JSON] [-callback 脚本名] [-pt 支付类型] [-ta 目标商户] [更多参数...]
  -bu         # 买家 ID（必填），两种格式：
              #   walnut:45g..67q 表示 Walnut 用户
              #   y8..r2:9ha..3ey 表示某域账户库的用户
  -fee        # 支付金额（必填），结尾三个大写字母表示货币单位（分），默认 RMB
  -cur        # 支付货币，默认 RMB（会强制大写）
  -br         # 简介，没有则根据字段生成
  -se         # 卖家域名，默认为操作账户所在域
  -meta       # 更多元数据 JSON；如果为空则从标准输入读取
  -callback   # 支付成功后的后续操作模板脚本的名称
  -pt         # 创建成功后立即向第三方平台发起支付（如 wx.qrcode）
  -ta         # 声明了 -pt 时必填，表示发送的目标商户
  # 更多参数    # 如 wx.scan 需要的付款码
```

# 示例

```bash
# 创建一个支付单
pay @create -br 测试支付 -bu walnut:xxx -fee 99

# 创建一个美元支付单
pay @create -br 测试支付 -bu walnut:xxx -se site0 -fee 199 -cur USD

# 创建一个带后续任务的支付单
pay @create -br 测试支付 -bu walnut:xxx -se site0 -fee 99 -callback abc

# 创建并发送一个支付单
pay @create -br 测试支付 -bu walnut:xxx -se site0 -fee 99 -pt zfb.qrcode -ta xxx
```
