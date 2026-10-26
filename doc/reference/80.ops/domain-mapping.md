---
title: 域名映射与路由
module: 80.ops
keywords: [/domain, hostmap, main-host, WalnutFilter, 域名路由, wn_www_grp]
source: [jetty/WalnutFilter.java, jetty/DmnMatcher.java, conf/hostmap]
related: [deploy, ../50.regapi/10-api-object.md]
---

# 域名映射与路由

`WalnutFilter`（web.xml 链首）负责按域名把请求路由到不同域的目录，是「多租户」的入口。源码：`src/com/site0/walnut/jetty/WalnutFilter.java`。

## 两级机制

### 1. 域名记录：/domain

`/domain` 目录下每个对象的**名字就是域名**，元数据决定路由：

- **`domain`（兼容旧键 `dmn_grp`）**：目标域（组名），写入请求属性 `wn_www_grp`
- **`site`（兼容 `dmn_site`）**：站点名，写入 `wn_www_site`
- **`tp`**：`A`（静态跳转/全路径重写）/ `B`（只记录域信息，路径不改）
- **`expi_at`**：过期时间（毫秒时间戳）；不设置 = 永久有效；过期显示「域名转发过期」错误页

添加映射：

```bash
echo '{}' > /domain/abc.com
o /domain/abc.com -e 'domain:abc; site:main; tp:B'
```

### 2. 路径重写：conf/hostmap

域名命中后，路径按 `conf/hostmap` 的正则表重写（规则自上而下，先中先得）：

```text
^/api/(.*)$      => /api/${grp}/${1}      # api.example.com/x → /api/example/x
^/(gu|www|a|o|u|p|theme|th|pay|_)/(.*)$  => /${1}/${2}        # 保留的系统前缀直通
```

`${grp}` 就是 /domain 记录里的域——这是「一个 API 目录服务多个域名」的关键：不同域名的请求被改写到 `/api/<各自域>/...` 下。

> www 模型已弃用：hostmap 默认最后一条 `^/(.*)$ => /www/${grp}/${1}` 是旧站点跳转，可按需改到自己的应用目录。

## main-host 白名单

`web.properties` 的 `main-host`（正则）匹配到的主机名**不做域名路由**（直接放行给后续处理器，如 localhost 开发调试）：

```properties
main-host=^(127.0.0.1|192.168.\d+.\d+|localhost)$
```

## 注入的请求属性

后续处理器（regapi、下载、websocket 等）从 request 属性拿路由结果：

- **`wn_www_path_org`**：原始路径
- **`wn_www_host` / `wn_www_ip` / `wn_www_port`**：请求主机/IP/端口
- **`wn_www_grp` / `wn_www_site`**：路由出的域与站点
- **`wn_www_path_new` / `wn_www_url`**：A 类跳转重写后的路径/完整 URL
- **`wn_www_static`**：A 类静态跳转标记

## 典型场景

```text
api.example.com/users/42
  → /domain/api.example.com {domain:example, tp:B}
  → hostmap ^/api/(.*)$ => /api/${grp}/$1   …不对，这里域名本身是 api.example.com
```

更常见的形态：

```text
www.shop-a.com/api/goods   →  {domain:shop-a}  →  /api/shop-a/goods
www.shop-b.com/api/goods   →  {domain:shop-b}  →  /api/shop-b/goods
```

一份 `/api` 代码（或 `_ANY` 通配），多域复用。

## 注意事项

- 域名记录对象名必须与实际 Host 头完全一致（无通配）；
- `expi_at` 适合做活动域名的定时下线；
- 调试时把自己的 IP/域名加进 `main-host` 可绕过路由直接访问本地路径。

## 延伸阅读

- 部署
- API 对象 — 路由之后的请求处理
