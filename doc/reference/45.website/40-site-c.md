---
title: C 类站：静态站点 + 动态 API
module: web
keywords: [C类站, 静态站点, SPA, index.html, wn_www_static, regapi, 客户端网站]
source:
  - web/WnMainModule.java
  - jetty/WalnutFilter.java
related: [10-domain-mapping, 20-site-a, 30-site-b, 10-api-object]
---

# C 类站概述

**C 类站是客户端网站的主流做法：域名挂静态前端，动态数据调域自己的 regapi 接口。**

一个域可以挂多个 C 类站：绑几个域名、放几个站点目录就行。

## 解决思路

C 类记录给请求打上标记 `wn_www_static=yes`，路径原样放行。Nutz 主模块
`WnMainModule` 的 `@At("/")` 和 `@At("/**")` 看到标记后，不再做登录跳转，
转去读站点目录（`handleRecordC`）：

```text
www.foo.com/order/12
  │  ① /domain 记录 {domain:foo, site:~/www/mysite, tp:C} 注入 wn_www_static=yes
  ▼
WnMainModule.handleRecordC
  ② 站点目录 = /home/foo/www/mysite
  ③ order/12 不是资源路径 → 回 index.html（SPA 回退）
```

## 域名记录配置

```bash
echo '{}' > /domain/www.foo.com
o /domain/www.foo.com -e 'domain:foo; site:~/www/mysite; tp:C'
```

**站点目录就是 `site`，按域的视角解析**（`HOME` 与 `PWD` 都是 `/home/<域>`）：
`~/www/mysite` 和 `www/mysite` 都指 `/home/foo/www/mysite`。

## 路径处理规则

对单页应用很友好：

- 空路径 → `index.html`
- `.js` `.js.map` `.css` `.json` `.html` `.txt` `.pdf` `.gif` `.png` `.svg` `.jpg` `.jpeg`
  后缀 → 原样取文件
- `assets/`、`resources/` 开头的路径 → 原样取文件
- 其余任何路径 → 一律回 `index.html`（路由交给前端 SPA）
- 文件不存在 → 404

响应走 `WnObjDownloadView`，支持 `If-None-Match`（ETag）和 `Range`（断点续传）。
C 类输出的是纯静态内容——`.html` 文件原样返回，不做任何模板渲染。

## 静态页面 + 动态接口：一个域名搞定

C 类不重写路径，`/api/...` 原样进 Nutz——**前端只要把域前缀写进路径，
就能直接调本域的 regapi 接口**：

```bash
curl http://www.foo.com/                    # → index.html
curl http://www.foo.com/order/12            # → index.html（SPA 回退）
curl http://www.foo.com/assets/app.js       # → 真实文件
curl http://www.foo.com/api/foo/goods/42    # → 域 foo 的 regapi 接口
```

`/api/foo/goods/42` 命中 `HttpApiModule`（`/api/<域>/<接口>`），
执行 `foo` 域 `~/.regapi/api/` 下的接口文件。接口的写法与鉴权全部复用
regapi 机制，见 [HTTP 注册接口（regapi）](../50.regapi/README.md)。

不把域前缀写进路径的话，也可以用 A 分支的 `api.foo.com` 子域接线
（见 [域名转发](10-domain-mapping.md) 的 A 分支），但单域名方案更省事。

## 一个域，多个站点

多条记录指向同一个域、不同的 `site`，就是一个域下的多个站点：

```bash
echo '{}' > /domain/foo.com
o /domain/foo.com -e 'domain:foo; site:~/www/mysite; tp:C'
echo '{}' > /domain/blog.foo.com
o /domain/blog.foo.com -e 'domain:foo; site:~/www/blog; tp:C'
```

两个域名共享域 `foo` 的数据与会话，展示各自的静态站。

## 注意事项

- SPA 回退把所有「非资源」路径都给 `index.html`——前端路由要自己兜住不存在的页面
- `www.` 前缀会被剥掉：`www.foo.com` 与 `foo.com` 命中同一条记录
- 静态站与动态接口共用域名时，接口路径的「域前缀」必须与记录里的 `domain` 一致

# 延伸阅读

- [域名转发](10-domain-mapping.md) — C 分支与 `/domain` 记录的机制
- [HTTP 注册接口（regapi）](../50.regapi/README.md) — 动态接口的机制
- [API 对象与元数据](../50.regapi/10-api-object.md) — 接口文件的写法
- [A 类站](20-site-a.md) — 动态渲染的另一种可能（搁置）
