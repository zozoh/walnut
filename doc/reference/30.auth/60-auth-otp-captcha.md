---
title: 验证码与 OTP
module: ext.data.vcode
keywords: [验证码, captcha, OTP, 动态码, 短信验证码, 邮箱验证码, vcode, 场景]
source:
  - ext/data/vcode/
  - ext/data/captcha/
related: [README, 10-auth-user-role, ../10.concepts/01-std-obj.md]
---

# 验证码与 OTP

登录前后的辅助认证：**图形验证码**（区分人和机器）与**动态码 OTP**
（短信/邮箱送达，确认「本人持有这个手机号/邮箱」）。

统一的验证码服务，核心约束只有一个：**一个账号在同一时刻只能进行一种验证**。
它不依赖会话——验证发生时，往往还没登录呢。

## 存储布局

验证码也是对象：

```bash
/var/vcode/{域}/{场景}/{账号}
# 域     ：操作者主组
# 场景   ：signup / login / chpasswd / captcha / 自定义
# 账号   ：手机号或邮箱（对象名）
```

```bash
/var/vcode/demo/captcha/13924452341     # 图形验证码（发给浏览器前的底码）
/var/vcode/demo/signup/13924452341      # 注册短信验证码
/var/vcode/demo/signup/abc@xyz.com      # 注册邮件验证码
```

```js
{
  nm      : "13910445672",  // 文件名即账号
  expi    : 1720000000000,  // 默认 10 分钟，过期自动清理
  v_code  : "839214",       // 验证码
  v_retry : 0,              // 已验证次数
  v_remax : 5               // 最大尝试次数
}
```

## 使用流程

```bash
# 1. 发图形验证码（HTTP 接口，浏览器直接 <img> 展示）
GET /u/vcode/captcha/get?d=demo&a=13910110055
# 服务器：生成 4 位数字 → 渲染 PNG 输出

# 2. 发手机验证码（先验图形验证码防机器人）
GET /u/vcode/phone/get?d=demo&s=signup&a=13910110055&t=6894

# 3. 校验（命令行；无任何输出=正确，e.cmd.vcode.check_fail=失败）
vcode check demo/signup/13910110055 839214
```

## 场景约定

- `captcha` — 图形验证码（发短信/邮件前的人机校验）
- `signup` — 注册
- `login` — 动态码登录（OTP 直接登录，`by_tp` 记为 `web_vcode`）
- `chpasswd` — 改密码

自定义场景名即可扩展（如 `bindphone`）。

## 命令

```bash
vcode check {域}/{场景}/{账号} {码}     # 校验（悄悄成功，报错失败）
captcha                                  # 图形验证码生成（man captcha）
```

## 注意事项

- **一发一验**：同账号并发获取会互相覆盖，多端同时验证可能失败——这是设计取舍
- 校验失败累计 `v_retry`，超 `v_remax` 本次验证码作废，需重新获取
- 短信发送通道在 root 身份下配置（`sms` 命令 + `~/.sms/`），验证码本身不含发送逻辑
- 不要用验证码做已登录后的频繁校验——已登录身份有会话，用 业务权限 控制动作即可

## 扩展阅读

- 过期对象机制 — 验证码的自动回收
- 会话管理 — 动态码登录成功后建会话
