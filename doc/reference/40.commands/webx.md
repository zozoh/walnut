---
title: webx 命令：站点用户自助操作
module: 40.commands
keywords: [webx, 站点登录, @auth, @session, @passwd, 微信登录, 动态密码]
source: [ext/net/webx/]
related: [../50.regapi/auth-history.md, net-cmd, weixin, ../30.auth/vcode-captcha.md]
---

# webx 命令：站点用户自助操作

`webx` 面向「站点用户」（在某个站点目录下注册的域账户）提供一整套自助操作：登录、会话查询、注销、改密、绑定手机/邮箱、验证码。它是 regapi 站点鉴权体系（`http-www-*`）的登录侧搭档。

- 源码：`src/com/site0/walnut/ext/net/webx/`（过滤器型命令）
- 手册：`man webx` 及子页（`man webx auth` 等）
- 用法骨架：`webx <站点目录> [@filter ...]`（如 `webx ~/www/website @auth ...`）

## 设计思路

1. **站点即目录**：一个站点对应一个目录对象，里面放着域账户库（登录选项 `WnLoginOptions` 存在站点目录的元数据/配置文件里）；
2. **多登录方式统一入口**：账密、邮箱、手机+动态密码、微信小程序 code、公众号 code 都是 `@auth` 的参数分支；
3. **票据即会话**：登录成功返回会话票据，后续请求带票据即被 regapi 认出（注入 `http-www-me-*`）；
4. **自助闭环**：改密、绑手机等敏感操作内置验证码/旧密码校验，不需要管理员介入。

## 适用场景

- **网站/App 登录注册**：小程序免密登录、账密登录、验证码登录
- **账户安全**：用户自助改密、绑定手机/邮箱
- **API 鉴权入口**：regapi 配 `http-www-home` 后，所有用户态接口的第一步

## 过滤器速查

- **`@auth`**：登录（账密/邮箱/手机/小程序/公众号），返回会话票据
- **`@session`**：查看当前会话信息
- **`@logout`**：注销会话
- **`@passwd`**：修改密码（自己或管理员代改）
- **`@captcha`**：发送/校验动态密码（短信/邮箱验证码）
- **`@bindphone` / `@bindemail`**：绑定手机/邮箱

## @auth：五种登录方式

```bash
# 1. 登录名 + 静态密码
webx ~/www/site @auth -name xiaobai -passwd 123456

# 2. 手机 + 动态密码（-strict 强制必须动态密码）
webx ~/www/site @auth -phone 13800001111 -captcha 3421
webx ~/www/site @auth -strict -phone 13800001111 -captcha 3421

# 3. 邮箱 + 动态密码
webx ~/www/site @auth -email a@b.com -captcha 3421

# 4. 微信小程序 code（-autoadd 用户不存在自动注册）
webx ~/www/site @auth -wxmp 0c1pDC0w3eFRB43MUc4w3zU8Y82pDC0T -autoadd
webx ~/www/site @auth -strict -wxmp <code>     # 严格模式：不自动注册

# 5. 公众号网页授权 code
webx ~/www/site @auth -wxgh <code>
```

## 会话与注销

```bash
webx ~/www/site @session tk_xxx        # 查看会话（用户/角色/变量）
webx ~/www/site @logout tk_xxx         # 注销
```

## 密码与绑定

```bash
# 改自己密码：旧密码必填
webx ~/www/site @passwd -oldpass oldPwd -newpass newPwd

# 域管理员代改（-name 指定目标用户）
webx ~/www/site @passwd -name zhangsan -newpass initPwd

# 绑定手机/邮箱（通常先 @captcha 发码校验）
webx ~/www/site @bindphone -phone 13800001111 -captcha 3421
webx ~/www/site @bindemail -email a@b.com -captcha 3421
```

## 完整登录流程（配合 regapi）

```text
前端                     Walnut
 |-- @auth（任一方式）---->| regapi: /api/site/login
 |<-- {ticket: tk_xxx} ----|
 |-- 业务请求 ?ticket=tk_xxx -->| regapi (http-www-home + http-www-auth)
                              |-- 注入 http-www-me-*，命令直接用
```

## 注意事项

- 站点目录的登录选项（是否允许注册、验证码方式等）由站点目录的配置决定（见 [站点鉴权](../50.regapi/auth-history.md)）；
- `-autoadd` 自动注册的用户记得配初始密码策略（`@passwd` 或首次登录引导）；
- 动态密码依赖 `vcode` 验证码体系（短信走 `sms`、邮箱走 `mailx`，见 [验证码](../30.auth/vcode-captcha.md)）。

## 延伸阅读

- [站点鉴权与历史](../50.regapi/auth-history.md) — 登录后的请求侧
- [网络命令组](net-cmd.md) — sms / mailx
- [微信集成](../70.integrations/weixin.md) — wxmp / wxgh 票据获取
