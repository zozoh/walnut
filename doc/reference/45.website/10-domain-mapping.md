---
title: 域名转发
module: web
keywords: [域名转发, /domain, hostmap, WalnutFilter, wn_www_grp, 静态站点, regapi, 动态渲染]
source:
  - jetty/WalnutFilter.java
  - jetty/DmnMatcher.java
  - web/WnMainModule.java
  - ext/data/domain/cmd_domain.java
  - conf/hostmap
related: [20-site-a, 30-site-b, 40-site-c, deploy, 02-domain-home, 10-api-object, 30-auth-in-domain]
---

# 域名转发概述

想让域里的应用挂到用户自己的域名上，让访客访问 `www.foo.com` 时看到的就是这个域的应用，
Walnut 的做法是**域名转发**：`web.xml` 过滤器链最前面的 `WalnutFilter`
（`src/com/site0/walnut/jetty/WalnutFilter.java`）拦下所有请求，按请求的 `Host` 头
找到对应的域，把请求路由过去。它是整个系统「多租户」的入口。

配合 Nutz 主模块 `WnMainModule`，它支持 **A / B / C** 三类转发，三类各自的
「站点形态」在本目录的三篇专题里：

- [A 类站](20-site-a.md)：动态渲染站点——原设想，未实现，设计桩子
- [B 类站](30-site-b.md)：管理后台的自定义登录页
- [C 类站](40-site-c.md)：静态站点——客户端网站的主流做法

**本文只讲转发机制本身：过滤器怎么认出域名、怎么把请求分流。**

## 要解决的问题

Walnut 里每个域（`/home/<grp>`）都有自己的数据和自己的 HTTP API
（[HTTP 注册接口（regapi）](../50.regapi/README.md)，存在域主目录 `~/.regapi/` 下）。

有了域名转发，管理员只要在 `/domain` 下放一条记录：

- `www.foo.com/index.html` → 域 `foo` 的静态站点
- `www.foo.com/api/foo/goods/42` → 域 `foo` 自己的 HTTP API（前端把域前缀写进路径）

用户在**自己的域名**下就有了完整的站点：静态页面由 C 类直接提供，
动态数据走该域的 regapi 接口——绑一个域名是一个站点，绑多个域名就是多个站点。

## 解决思路

每个请求在进入 Nutz 主模块之前，都要先过一遍 `WalnutFilter`：

```text
HTTP 请求
  │
  ▼
WalnutFilter（/* 链首）
  ① 取 Host 头：剥掉端口、剥掉 www. 前缀
  ② 命中 web.properties 的 main-host？ ── 是 ──▶ 放行（不路由）
  ③ 查 /domain/<host> 记录
       ├─ 没有记录 ───────────────▶ 放行
       ├─ expi_at 已过期 ─────────▶ errorPage（域名转发过期）
       └─ 按记录的 tp 分流：
            tp:B ─▶ 注入域/站点属性后放行（后续模块自己消费）
            tp:C ─▶ 打静态标记，交给 WnMainModule 读静态目录
            其他 ─▶ 按 hostmap 重写路径，服务器端 forward 过去
```

一句话：**过滤器负责「认出域名、决定怎么走」，路由之后干什么，是各模块自己的事。**

## 三类转发：A / B / C

`tp` 决定请求在过滤器里走哪条路：

- **A（缺省）**：按 hostmap 重写路径，服务器端 forward 过去——机制见下文
- **B**：不改路径，注入 `wn_www_grp` / `wn_www_site` 后放行
- **C**：不改路径，多注入一个 `wn_www_static=yes`，交给 `WnMainModule`

B 和 C 在过滤器里就结束了；A 的细节最多，单独一节。

## 域名记录：/domain

`/domain` 下每个对象的名字就是**域名**，元数据决定怎么路由。
目录在首次路由时由过滤器自动创建（`createIfNoExists`）。

- **对象名**：域名本身。必须与请求 `Host` 头剥掉端口、剥掉 `www.` 前缀后的结果
  **完全一致**，不支持通配。所以 `www.foo.com` 和 `foo.com` 命中同一条记录
- **`domain`**（兼容旧键 `dmn_grp`）：目标域，即 `/home/<domain>` 的组名；
  路由后写入请求属性 `wn_www_grp`
- **`site`**（兼容旧键 `dmn_site`）：站点路径，写入 `wn_www_site`。
  C 类把它当静态目录（相对域主目录解析，`~/www/mysite` 即 `/home/<域>/www/mysite`）；
  登录站点机制也用它定位站点配置（见 [域内用户存储与认证](../30.auth/30-auth-in-domain.md)）
- **`tp`**：转发类型 `A` / `B` / `C`。**不设置时按 A 处理**
- **`expi_at`**：过期时间（毫秒时间戳）。不设置或为 0 表示永久有效；
  过期请求被转发到错误页（`err_message=域名转发过期`）

`/domain` 属系统区，只有 root 组可写。日常管理就是直接增删对象，
`domain @list` 可以列出全部记录（列：`nm` / `tp` / `domain` / `site` / `expi` / `ct` / `lm`）：

```bash
# 把 www.foo.com 绑到 foo 域（C 类：静态站点）
echo '{}' > /domain/www.foo.com
o /domain/www.foo.com -e 'domain:foo; site:~/www/mysite; tp:C'

# 查看 / 下线
domain @list
o /domain/www.foo.com -del

# 活动域名限时下线：到点后全部请求进入错误页
o /domain/promo.foo.com -e 'expi_at:1790000000000'
```

## A 分支：路径重写

记录没有 `tp`，或 `tp` 不是 B/C 时，请求走 A 分支：过滤器把路径按 `hostmap`
文件里的规则**自上而下逐条匹配，第一条命中的规则胜出**，把路径渲染成新路径，
再服务器端 forward 过去（`RequestDispatcher`，浏览器无感知，地址栏不变）。

模板可以用的变量：

- `${0}` ～ `${n}`：正则的捕获组，`${0}` 是完整匹配
- `${grp}`：记录里的域

默认 `conf/hostmap` 的关键规则：

```text
^/(gu|www|a|o|u|p|theme|th|pay|_)/(.*)$  => /${1}/${2}     # 系统前缀原样直通
^/api/(.*)$           => /api/${grp}/${1}                 # 动态接口
^/(.*)$               => /www/${grp}/${1}                 # 兜底
```

**`/api` 规则是 A 分支当前的主要用途**：`api.foo.com/goods/42` 被重写成
`/api/foo/goods/42`，由 `HttpApiModule`（`@At("/api")`）执行——`foo` 是域，
`goods/42` 是该域 `~/.regapi/api/` 下的 API 对象。接口文件的写法见
[API 对象与元数据](../50.regapi/10-api-object.md)。

没有规则命中的请求原样放行；重写结果以 `/websocket` 开头时也跳过 forward，原样放行。

最后一条兜底规则把路径重写到 `/www/<域>/...`。当前代码库里已没有任何模块处理
`/www/` 前缀（www 站点模型随 `cmd_www` 删除），这类请求落到 `WnMainModule` 的
`@At("/**")` 兜底入口，抛 `Invalid Req`，响应为 200 空内容，错误只出现在日志里。

A 分支对应的站点形态（服务端动态渲染）设想已搁置，见 [A 类站](20-site-a.md)。

## B 分支：只注入，不重写

`tp:"B"` 的记录表示「这个域名的路径不用改，但后面的模块需要知道当前是哪个域」。
过滤器把 `wn_www_grp`、`wn_www_site` 注入请求属性后原样放行。

**B 类站今天的定位是管理后台的自定义登录页**，登录页的渲染与查找顺序见
[B 类站](30-site-b.md)；背后的登录站点机制见
[域内用户存储与认证](../30.auth/30-auth-in-domain.md)。

## C 分支：静态标记

`tp:"C"` 的记录给请求打上 `wn_www_static=yes` 标记，路径原样放行。
`WnMainModule` 看到标记后不再做登录跳转，转去读站点目录里的静态文件。

**C 类站是客户端网站的主流做法**，路径规则与「静态站 + 动态 API」的配方见
[C 类站](40-site-c.md)。

## 注入的请求属性

过滤器**总会**注入：

- **`wn_www_path_org`**：原始路径
- **`wn_www_host`**：剥掉端口与 `www.` 前缀的域名
- **`wn_www_ip`**：对方 IP
- **`wn_www_port`**：服务端口

命中记录后**还会**注入：

- **`wn_www_grp`**：记录里的域（B/C 直接注入；A 在重写成功后注入）
- **`wn_www_site`**：记录里的站点路径
- **`wn_www_path_new`** / **`wn_www_url`**：A 分支重写后的新路径 / 原始完整 URL
- **`wn_www_static`**：`yes` 表示 C 类静态站点请求，`WnMainModule` 据此读静态目录

## 配置参考

### web.xml 过滤器参数

```xml
<filter>
   <filter-name>walnutFilter</filter-name>
   <filter-class>com.site0.walnut.jetty.WalnutFilter</filter-class>
   <init-param><param-name>nutzFilterName</param-name><param-value>nutz</param-value></init-param>
   <init-param><param-name>errorPage</param-name><param-value>/WEB-INF/jsp/invalid_domain.jsp</param-value></init-param>
   <init-param><param-name>hostMap</param-name><param-value>hostmap</param-value></init-param>
</filter>
<filter-mapping>
   <filter-name>walnutFilter</filter-name>
   <url-pattern>/*</url-pattern>
</filter-mapping>
```

- **`nutzFilterName`**：取 Nutz Ioc 容器用的过滤器名，固定 `nutz`
- **`errorPage`**：域名记录过期时展示的错误页
- **`hostMap`**：hostmap 文件名，缺省 `hostmap`；经 Nutz `Files.findFileAsStream` 读取
  （classpath 优先，文件系统兜底）

**这个映射没有声明 FORWARD 派发**，所以过滤器只在原始请求时跑一次：A 分支 forward 出来的
新路径不会再次进入域名路由；nutz 过滤器声明了 FORWARD，新路径能继续被 Nutz 处理。

### main-host：哪些域名不路由

`web.properties` 的 `main-host` 是一个正则，命中的域名**不参与域名转发**，直接放行：

```properties
main-host=^(127.0.0.1|192.168.\d+.\d+|localhost|ngrok[.].+)$
```

本地开发、负载均衡探活等主机名加进这里即可。**不配置这个项时，任何域名都不路由**
（过滤器全部放行）。

### hostmap 规则格式

- 一行一条 `正则 => 模板`，空行与 `#` 注释忽略
- 自上而下匹配，先中先得
- 模板支持 `${0}` ～ `${n}`（捕获组）和 `${grp}`（域）
- 重写结果以 `/websocket` 开头时跳过 forward，原样放行

## 注意事项

- 域名对象名必须与 `Host` 头剥掉端口、剥掉 `www.` 前缀后**完全一致**，不支持通配
- `tp` 缺省走 A 分支；新域名建议显式写 `tp:C`（静态站）或 `tp:B`（登录页）
- A 类站（动态渲染）设想已搁置，代码里没有实现；A 分支兜底规则
  `^/(.*)$ => /www/${grp}/${1}` 指向已删除的 www 模型：命中后是 200 空响应 + 日志 `Invalid Req`
- B/C 分支完全不看 hostmap；C 类域名上的 `/api/...` 不会被重写——
  前端把域前缀写进路径（`/api/<域>/...`）即可调本域 API，见 [C 类站](40-site-c.md)
- C 类的 `site` 按域主目录解析（`~/` 与相对路径都相对 `/home/<域>`）
- `/domain` 属系统区（root 组可写），普通域用户不能自己绑域名
- 调试时把自己的 IP/域名加进 `main-host`，可绕过路由直接访问原始路径

## 主要代码类

- `jetty/WalnutFilter.java`：过滤器本体——/domain 查记录 + A/B/C 分流
- `jetty/DmnMatcher.java`：hostmap 的一条规则（正则 + 模板）
- `web/WnMainModule.java`：`handleRecordC`——C 类静态资源的实际提供者
- `ext/sys/httpapi/HttpApiModule.java`：`/api/<域>/...` 的执行器（regapi）——A 分支重写与 C 类直调殊途同归
- `web/module/AppModule.java`：B 分支属性的消费方（`/a/login` 域定制登录页）
- `login/site/WnLoginSite.java`：按 `/domain/<host>` 找登录站点配置
- `ext/data/domain/cmd_domain.java`：`domain @list` 管理命令

# 延伸阅读

- [网站机制概述](README.md) — A/B/C 三类站的整体导览
- [A 类站](20-site-a.md) — 动态渲染站点的设计桩子
- [B 类站](30-site-b.md) — 管理后台的自定义登录页
- [C 类站](40-site-c.md) — 静态站点 + 动态 API 的完整做法
- [部署与运行配置](../100.ops/deploy.md) — web.xml 过滤器链的全貌
- [Domain 与 Home](../10.concepts/02-domain-home.md) — 域是什么、主目录怎么展开
- [HTTP 注册接口（regapi）](../50.regapi/README.md) — 动态接口的机制
- [域内用户存储与认证](../30.auth/30-auth-in-domain.md) — 登录站点如何复用 `site` 配置
- [系统管理命令](../100.ops/sys-admin.md) — 域名记录的管理操作
- [排查手册](../100.ops/troubleshooting.md) — `域名转发过期` 等错误排查
