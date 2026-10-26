---
title: 支付宝与阿里云
module: 70.integrations
keywords: [alipay, alipayconf, aliyunoss, aliyunvod, OSS, VOD, RAM]
source: [ext/net/alipay/, ext/net/aliyun/]
related: [payment, xapi, sms-mail]
---

# 支付宝与阿里云

## 支付宝（alipay）

> 无手册页，用法以源码为准（`ext/net/alipay/`，两个处理器 `pay` / `payre`）。

命令 HOME 目录下要有一个 `alipayconf` 配置文件（JSON）：

```js
{
  partner        : "2088...",      // 合作身份者ID（签约账号）
  key            : "xxx",          // MD5 密钥（sign_type=MD5 时）
  privateKey     : "...",          // RSA 私钥（sign_type=RSA 时）
  sign_type      : "MD5",          // 签名方式
  payment_type   : "1",
  pay_notify_url : "https://host/api/pay/zfb/re",   // 异步回调
  pay_return_url : "https://host/pay/done"          // 同步跳转
}
```

```bash
alipay <confHome> pay     # 从 stdin 读支付参数（out_trade_no/amount...），输出生成的支付表单/链接
alipay <confHome> payre   # 从 stdin 读回调请求，验签并解析
```

> 这是老的支付宝即时到账接口（mapi 网关）。新对接建议优先用 支付 的 `zfb.qrcode/zfb.scan` 渠道。

## 阿里云 OSS（aliyunoss）

配置 `~/.aliyun/oss/<名称>.json`（RAM 子账号）：

```js
{
  endpoint   : "oss-cn-qingdao.aliyuncs.com",
  id         : "ABC",
  secret     : "ABCDEFG",
  bucketName : "testoss"
}
```

子命令（均有手册页）：

```bash
aliyunoss testoss upload    js/jquery/jquery.js ~/jquery_local.js
aliyunoss testoss download  js/jquery/jquery.js ~/jquery_local.js
aliyunoss testoss lsdir     js/
aliyunoss testoss meta      js/jquery/jquery.js -json
aliyunoss testoss mkdir     newdir/
aliyunoss testoss rm        old.js
aliyunoss testoss url       js/jquery/jquery.js   # 生成访问 URL（含签名）
```

## 阿里云 VOD（aliyunvod）

配置 `~/.aliyun/vod/<名称>.json`（字段同 RAM id/secret + regionId 等，见 `man aliyunvod`）：

```bash
aliyunvod <名称> playauth  -videoId xxx          # 获取播放凭证（前端 Aliplayer 用）
aliyunvod <名称> playinfo  -videoId xxx          # 播放信息
aliyunvod <名称> search    -kw '标题关键字'       # 搜索媒体
aliyunvod <名称> video     -videoId xxx          # 视频详情
```

## 典型场景

- **静态资源加速**：`aliyunoss upload` 发布 → `url` 取链 → 前端 CDN；
- **视频站**：`aliyunvod playauth` + 前端 Aliplayer 播放；
- **支付回调**：regapi 收 `pay_notify_url` → `pay re`（见 支付）。

## 注意事项

- RAM 子账号最小权限原则：OSS 只给单 bucket 读写，VOD 只给点播权限；
- `endpoint` 内外网要分清（服务器上写内网 endpoint 免流量费）；
- 密钥配置放 root 账户下，普通用户通过命令参数引用配置名。

## 延伸阅读

- 支付
- xapi — 阿里云更多 OpenAPI 可走 xapi 专家配置
