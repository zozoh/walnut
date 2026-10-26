---
title: 安全注意事项
module: 80.ops
keywords: [安全, secret, 权限, root, api-access-token, https, 注入]
source: [web/, ext/sys/httpapi/, api/auth/]
related: [deploy, sys-admin, ../30.auth/0-auth-pvg.md]
---

# 安全注意事项

Walnut 的安全模型：域隔离 + 元数据权限 + 会话票据。本节是上线前的安全清单。

## 密钥存放约定

- **MQ 发送密钥** — 位置：`~/.mq/secret` — 说明：`mq secret` 生成
- **API 访问令牌** — 位置：API 对象 `api-access-token` 元数据 — 说明：客户端经参数携带
- **邮箱 SMTP 密码** — 位置：`~/.mail/<账户>.json`
- **短信 apikey** — 位置：`~/.sms/config_<provider>`
- **阿里云 RAM 密钥** — 位置：`~/.aliyun/oss — 说明：vod/<名>.json`
- **微信 appsecret** — 位置：`~/.xapi/<平台>/<账号>/config.json5` — 说明：见 xapi
- **xapi 动态票据** — 位置：`access_token` 文件 — 说明：框架自动维护

原则：**密钥集中放 root 家目录**，业务用户通过任务身份借用；不要把含密钥的对象放到公共目录。

## 权限边界

- `/sys`（调度/钩子/锁）、`/var`（支付单等）、`/domain`（域名路由）是系统区，**只有 root 组可写**；普通域用户写入依赖 元数据权限 拦截；
- `hook_user` 只允许在全局钩子上使用（用户钩子强制以属主身份执行，防止提权）；
- `mgadmin`（裸 Mongo 通道）只给 root 组。

## API 面防护

- **令牌**：对外接口一律 `api-access-token`，不要只靠“路径不公开”；
- **注入**：用户输入进命令模板的位置用 `http-safe-params` 白名单约束；JS 钩子里拼接命令前过滤参数；
- **鉴权**：涉及用户数据的接口配 `http-www-home` + `pvg-assert`（见 站点鉴权）；
- **缓存**：需要鉴权的请求自动无视缓存（`cache-*` 与 `http-www-auth` 互斥，见 API 缓存）。

## 传输与会话

- 生产环境必须 HTTPS（票据 `http-qs-ticket`、会话 cookie 走明文等于裸奔）；
- `root-init-passwd` 上线即改；`resetrootpasswd` 命令可重置；
- 会话时长 `se-sys-du`（系统会话）与 `se-tmp-du`（API 临时对象）按业务调整，别为了“方便”设无限长；
- 黑名单角色（`!` 前缀）慎用，误配会把自己锁在门外——留一个 root 后门账号。

## 已弃用组件的提示

- **www 站点模型**已删除（`cmd_www` 不存在）：hostmap 里的 `/www/${grp}` 规则和 webx 的站点目录用法属于遗留，新功能不要依赖；
- **Thing 数据集**已弃用（仅维护）：遇到 `thing_*` 报错的老文档/老脚本，应迁移到 `o + race + WnQuery` 体系；
- **微信支付实现**源码停用（`.java.txt`），启用前需补商户配置改造（见 支付）；
- **history 写库**未启用（`_record_history` 主体被注释，见 站点鉴权）。

## 上线检查清单

- [ ] root 密码已改，root-init-passwd 已失效化
- [ ] HTTPS 强制
- [ ] 对外 API 都有 api-access-token 或 pvg 断言
- [ ] 密钥都在 root 家目录，对象权限已收紧
- [ ] main-host 白名单不含公网 IP（防止绕过域名路由）
- [ ] cron 任务以最小权限用户执行

## 延伸阅读

- 账户与权限
- 自动登录与票据
- 部署
