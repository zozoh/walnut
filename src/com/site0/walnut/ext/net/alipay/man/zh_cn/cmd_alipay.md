# 命令简介

`alipay` 支付宝即时到账接入

每个支付配置是一个主目录：`~/.alipay/<别名>/alipayconf`（JSON 配置）。
第一个参数是别名（`.` 表示当前目录），第二个参数是处理器。

# 用法

```bash
alipay [别名|. ] @处理器 [参数...]
```

支持的处理器：

```bash
@pay     # 生成支付请求（跳转链接/表单）
@payre   # 接收并校验支付回调
```

# 示例

```bash
# 用管道里的支付单 JSON 生成支付请求
echo '{out_trade_no:"T20261023",subject:"订单",total_fee:"0.01"}' | alipay myshop @pay

# 支付单来自文件
alipay myshop @pay ~/payment/order.json
```
