---
title: 自动登录与 Token 机制
module: login
keywords: [自动登录, ackey, 签名, token, nonce, timestamp, 防重放, 第三方对接]
source:
  - web/module/UsrModule.java
  - ext/sys/httpapi/hdl/
related: [session, accounts, api-object]
---

# 自动登录与 Token 机制

两类免密身份凭证，用于与第三方系统对接。

## 一、ackey 自动登录（用户级）

用户配置里存一对密钥，第三方凭**签名**换登录态。

### 密钥配置

```bash
usermod demo -E '{"ackey":"`uuid`","ackey_timeout":1800}'
# ackey_timeout：签名有效秒数，0=禁用，-1=永不超时
```

### 签名算法

```bash
sign = sha1( ackey + "," + user + "," + time + "," + once )
# user : 对接用户名（小写）
# time : 毫秒时间戳（两端服务器时间必须同步）
# once : 一次性随机值（推荐 uu32/uu16）
```

### 调用

```bash
curl 'http://host:8080/u/do/login/auto?sign=037b..&user=demo&time=1456453615904&once=s3pp..'
# 成功：写会话 cookie 并跳 target（可选参数），失败显示原因
```

Java 生成示例：

```java
String ackey = "33khmpoe9cjhno207gis249fau";
String user  = "demo";
long   time  = System.currentTimeMillis();
String once  = R.UU64();
String sign  = Lang.sha1(ackey + "," + user + "," + time + "," + once);
String url   = "http://host/u/do/login/auto?sign=" + sign
             + "&user=" + user + "&time=" + time + "&once=" + once;
```

失败原因：未启用 ackey / 签名错误 / 时间戳过期（两端时间不同步最常见）。

## 二、Token 机制（应用级，开放 API 用）

给「不能传账号密码」的调用方使用：

```bash
客户端持有：appid + token（密钥，绝不发送） + 时间戳 + nonce
请求携带：  appid, timestamp, nonce, sign, 业务参数
sign = sha256(nonce, timestamp, token, 业务参数...)
服务端：按 appid 查 token，重算 sign 比对
```

**为什么需要 nonce + timestamp（防重放）**：

- 只验 sign：请求被截获原样重发照样合法。
- nonce 一次性：服务器记用过的 nonce，二次出现即拒绝。
- nonce 不能永久存：配合 timestamp（要求双方时间差 ≤10 分钟），nonce 只存 20 分钟——过期 nonce 和过期时间戳双重失效。

密钥分发必须走安全信道（HTTPS 或线下手敲），参见微信/支付宝的做法。

## 选型

- **第三方系统免密跳入 Walnut 界面**：ackey 自动登录
- **服务端对服务端的 API 调用**：Token + sign
- **用户手持设备访问**：正常登录 + 会话票据（见 [session](session.md)）

## 注意事项

- `ackey_timeout:-1` 的签名长期有效——一旦泄露永远可登录，务必定期轮换。
- 两端时间同步是硬要求（NTP），否则签名大面积过期。
- 所有这类 URL 必须 HTTPS 传输，签名 URL 出现在浏览器地址栏即视为泄露。
