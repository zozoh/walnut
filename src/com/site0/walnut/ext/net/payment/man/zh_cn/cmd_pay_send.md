# 过滤器简介

`@send` 将一个支付单信息真正执行与第三方支付平台交互的操作。

# 用法

```bash
pay @send 支付单ID 支付类型 目标 [更多参数...]
       # 支付单ID    # 支付单的 ID
       # 支付类型    # wx.qrcode | wx.jsapi | wx.scan | zfb.qrcode
       # 目标        # 支付目标（如微信公众号、支付宝目录等）
       # 更多参数    # 如 wx.scan 需要的付款码
```

支付类型支持：

- `wx.qrcode`：微信主动扫付款码
- `wx.jsapi`：微信公众号支付
- `wx.scan`：微信被物理码枪扫码支付
- `zfb.qrcode`：支付宝主动扫付款码

命令输出：

```js
{
    status : "WAIT",        // 支付状态：OK 成功 | FAIL 失败 | WAIT 等待用户确认
    changedKeys : [..],     // 改变了支付单哪些字段
    type : "LINK",          // 内容类型：LINK 付款链接 | QRCODE 二维码 | JSON 配置对象
    data : ..               // 根据 type 决定：URL、二维码内容或 Map
}
```

# 示例

```bash
# 发起微信主动扫码支付
pay @send 4g..3a wx.qrcode xxx

# 发起微信公众号支付
pay @send 4g..3a wx.jsapi xxx

# 发起微信被物理码枪扫码支付
pay @send 4g..3a wx.scan xxx 238..871

# 发起支付宝主动扫码支付
pay @send 4g..3a zfb.qrcode xxx
```
