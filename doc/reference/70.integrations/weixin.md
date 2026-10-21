---
title: 微信集成
module: 70.integrations
keywords: [微信, wxmp, wxgh, wxjssdk, access_token, 小程序登录, 公众号]
source: [ext/net/webx/, ext/net/xapi/hdl/xapi_wxjssdk.java, ext/net/payment/weixin/]
related: [payment, xapi, ../40.commands/net-cmd.md]
---

# 微信集成

> ⚠️ **现状说明**：项目里没有独立的 `weixin` 命令，也没有 `~/.weixin` 配置目录的专属处理代码。当前源码中微信能力集中在三处：站点登录票据、JSSDK 签名（走 xapi 专家配置）、微信支付实现（已停用）。本文按源码现状描述。

## 1. 微信登录（站点账号）

`webx` 命令支持用微信票据登录站点账号（见 [网络命令](../40.commands/net-cmd.md)）：

```bash
# 微信小程序 code 换登录态（用户不存在可 -autoadd 自动建号）
webx ~/www/website @auth -wxmp 0c1pDC0w3eFRB43MUc4w3zU8Y82pDC0T -autoadd

# 严格模式：用户不存在则报错（配合先注册流程）
webx ~/www/website @auth -strict -wxmp <code>

# 公众号（服务号/订阅号网页授权）票据
webx ~/www/website @auth -wxgh <code>
```

登录成功后，regapi 侧通过 `http-www-*` 元数据拿到用户信息（见 [站点鉴权](../50.regapi/auth-history.md)）。

## 2. JSSDK 签名

`xapi wxjssdk` 生成微信 JS-SDK 的注入配置（appId/timestamp/nonceStr/signature）：

```bash
xapi wxjssdk wxgh gh_xxxx jssdk -url https://site0.cn/page/demo
# 输出可直接注入页面的 JSON 配置
```

- `wxgh` 是专家配置名（在 xapi 专家配置里定义 appid/secret 与 jssdk 请求）；
- 内部自动维护 access_token（动态票据，见 [xapi](xapi.md) 的 `accessKeyRequest`）；
- `-force` 强制刷新 access_token。

## 3. 微信支付（源码停用）

`ext/net/payment/weixin/` 下有三种微信支付的实现文件，但都被改名为 `.java.txt` 后缀——**不参与编译**，即当前版本微信渠道不可用：

```text
WxQrcodePay3x.java.txt   扫码付
WxJsApiPay3x.java.txt    公众号内支付
WxScanPay3x.java.txt     付款码被扫
```

如需启用：恢复文件名 → 补齐商户配置（mch_id/API 证书等，需按微信支付 V3 接口改造）→ 在 `WnPays` 体系注册。详见 [支付](payment.md) 的渠道状态表。

## 典型场景

- **小程序用户免注册登录**：小程序 `wx.login` 拿 code → 后端 regapi 调 `webx @auth -wxmp` → 返回站点票据；
- **公众号网页调起 JSSDK**：regapi 里 `xapi wxjssdk` → 模板渲染进页面；
- **微信模板消息**：无内置命令，通过 `xapi send` 调用专家配置里的模板消息 API。

## 延伸阅读

- [xapi](xapi.md) — 专家配置机制（微信接入都走它）
- [支付](payment.md)
- [站点鉴权](../50.regapi/auth-history.md)
