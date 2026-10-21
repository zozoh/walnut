---
title: ThirdXApi：第三方 API 框架
module: 70.integrations
keywords: [xapi, 专家配置, expert, accessKey, @AK, 动态票据, requests]
source: [ext/net/xapi/]
related: [weixin, alipay-aliyun, ../40.commands/net-cmd.md]
---

# ThirdXApi：第三方 API 框架

xapi 是 Walnut 调用第三方 HTTP API 的通用框架：不维护 SDK，用**专家配置（Expert）**描述一个平台的 base URL、密钥管理、请求表。源码在 `src/com/site0/walnut/ext/net/xapi/`。

## 框架结构

```text
ThirdXApi
|-- experts : ThirdXExpertManager      专家配置（平台级）
|-- configs : ThirdXConfigManager      账号配置（密钥级）
```

内置专家在 `xapi/data/*.json`（wxgh/wxmp/chatgpt/tianyancha/youtube/fb-graph/gpt4），运行时按以下顺序查找：

1. `~/.xapi/experts/<apiName>.expert.json` —— 域自定义专家（优先）；
2. 内置默认专家。

## 专家配置结构

以内置 `wxgh`（微信公众号）为例：

```js
{
  "base"   : "https://api.weixin.qq.com/",   // 请求路径会自动拼合 base
  "timeout": 3000,
  "connectTimeout": 1000,
  "home"   : "~/.xapi/wechat-gh",            // 账号配置主目录
  "configFilePath"    : "config.json5",       // 账号配置文件名（在 ${home}/${account}/ 下）
  "accessKeyFilePath" : "access_token",       // 密钥缓存文件名
  "accessKeyRequest"  : {                     // 【动态票据】如何获取密钥
    "path"  : "cgi-bin/token",
    "method": "GET",
    "params": { "appid": "=appID", "secret": "=appsecret" },
    "dataType": "json"
  },
  "accessKeyObj"      : {                     // 从票据响应里取哪些字段
    "ticket"       : "=access_token",
    "expiTime"     : "=expires_in?7200",      // ? 表示默认值
    "expiTimeUnit" : "s"
  },
  "requests": {                               // 请求表：键即 API 名
    "gh_user_info": {
      "path"  : "cgi-bin/user/info",
      "method": "GET",
      "params": { "access_token": "=@AK", "openid": "=openid" }   // =@AK 自动带票据
    },
    "wxacode_get_unlimited": {
      "path": "wxa/getwxacodeunlimit", "method": "POST",
      "bodyType": "json", "body": { "scene": "=scene" },
      "dataType": "jpeg"                      // 响应是图片
    },
    "jssdk": { /* ... 带 cache 配置 ... */ }
  }
}
```

值里的 `=` 前缀是**模板占位语法**：`=appID` 从账号配置取键，`=@AK` 自动注入当前票据，`?默认值` 兜底。

## 账号配置

每个平台账号一个目录：`${home}/${account}/`：

```text
~/.xapi/wechat-gh/
  gh_xxxx/                    # 账号名
    config.json5              # appID / appsecret 等静态配置
    access_token              # 动态票据缓存（自动维护，勿手改）
```

## 命令

```bash
xapi info              # 列出所有可用 API（专家+请求）
xapi send wxgh gh_xxxx gh_user_info -vars '{openid:"xxx"}'
xapi req  wxgh gh_xxxx gh_user_info -vars '{openid:"xxx"}'   # 只看请求，不发送
xapi ak   wxgh gh_xxxx                                       # 检查密钥可用性
xapi wxjssdk wxgh gh_xxxx jssdk -url https://...            # JSSDK 签名专用
```

## 缓存

请求可声明 `cache`（见 jssdk 示例）：对 path/params 做键缓存响应，配合 `expiIn` 控制有效期——适合 ticket、getticket 这类低频变化的数据。

## 典型场景

- **接新平台**：写一份 `~/.xapi/experts/myapi.expert.json` + 建账号目录 → 立即 `xapi send` 可调；
- **多账号**：同专家不同 account 目录（生产/测试密钥隔离）；
- **敏感操作先 dry-run**：`xapi req` 看最终请求体。

## 注意事项

- `access_token` 等票据文件由框架自动刷新，多节点部署时注意各节点各自缓存（或共享 home）；
- 请求参数值都会被模板渲染，字符串含 `=` 开头要注意转义；
- 内置专家（chatgpt/tianyancha 等）只是配置模板，账号配置仍需自建。

## 延伸阅读

- [微信](weixin.md) — wxgh/wxmp 专家的实际应用
- [网络命令](../40.commands/net-cmd.md) — httpc（不想配专家时的轻量替代）
