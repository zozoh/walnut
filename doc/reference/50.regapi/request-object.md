---
title: 请求对象：一次请求一个对象
module: 50.regapi
keywords: [http-api 对象, 请求元数据, http-qs, 上传下载, http-body, http-output]
source: [ext/sys/httpapi/]
related: [api-object, path-params, ../10.concepts/02-std-obj.md]
---

# 请求对象：一次请求一个对象

Walnut 把**每个 HTTP 请求也变成一个临时对象**（即 http-api 对象，tree=`http-api`）。命令和 JS 通过读取这个对象的元数据拿到请求的一切信息——这贯彻了「一切皆对象」的设计。

## 请求对象长什么样

执行 API 命令时，工作目录里会出现这个临时对象（可用 `o id:xxx` 查看），它的元数据大致是：

```js
{
  http-url      : "http://host/api/hello",     // 完整 URL
  http-uri      : "/api/hello",               // 路径部分
  http-method   : "POST",
  http-qs-name  : "zozoh",                    // 每个查询参数一个元数据
  http-qs-mime  : "text/plain",
  http-header-USER-AGENT : "curl/8.x",        // 每个请求头一个大写元数据
  http-cookie-SESSION      : "abc...",        // 每个 Cookie 一个大写元数据
  http-remote-addr : "127.0.0.1",
  http-remote-host : "localhost",
  http-remote-port : 5210,
  // 会话信息（见 30.auth）
  http-usr      : "zozoh",                    // 登录用户（用户名或 id:）
  http-grp      : "demo",                     // 登录用户的域
  http-se-ticket : "tk...",                   // 系统会话票据
  http-se-me-name : "zozoh",
  http-se-me-group: "demo",
  http-se-vars  : "{...}",                    // 会话变量 JSON
  // 站点会话（http-www-auth 时注入，见 auth-history.md）
  http-www-me-id : "...", http-www-me-nm : "...",
  http-www-me-main : "...", http-www-me-email : "...",
  http-www-me-phone : "...", http-www-me-group : "...",
  http-www-se-ticket : "...", http-www-ticket : "..."
}
```

## 常用元数据速查

- **`http-url` / `http-uri`**：完整 URL / 路径
- **`http-method`**：GET / POST / ...
- **`http-qs-<名>`**：查询参数（小写原名）
- **`http-header-<NAME>`**：请求头（**大写**，如 `http-header-CONTENT-TYPE`）
- **`http-cookie-<NAME>`**：Cookie（**大写**）
- **`http-usr` / `http-grp`**：当前登录用户与域
- **`http-se-*`**：系统会话（见 [会话](../30.auth/session.md)）
- **`http-www-*`**：站点会话（见 [鉴权与历史](auth-history.md)）
- **`http-remote-addr/host/port`**：客户端网络信息

## 在命令/JS 中读取

```bash
# 命令里直接用 ${http-qs-xxx} 占位符
echo '{"who":"${http-qs-name}","ua":"${http-header-USER-AGENT}"}'

# JS 里通过上下文对象
var oReq = sys.io.readPath(".");
print(JSON.stringify({
  who : oReq.getString("http-qs-name"),
  ua  : oReq.getString("http-header-USER-AGENT")
}));
```

## POST body 就是文件内容

`http-body:text` 时，请求的 body 直接成为请求对象的**文件内容**：

```bash
# API 配置：o xxx -e 'http-body:text;http-output:text'
# 客户端：curl -d 'raw text here' http://host/api/echo
cat -                                                    # 命令从 stdin 读的就是 body
```

`http-body:json` 时，body 解析成 JSON，字段并入参数表（配 `http-post-json-merge:true` 更显式）。

## 上传与下载

**上传**（`http-body:form`，multipart 表单）：每个文件字段变成一个 `id:xxx` 临时文件，命令里 `cp` 走：

```bash
# 客户端：curl -F 'file=@a.jpg' -F 'title=hi' http://host/api/upload
cp id:${http-qs-file} ~/uploads/${http-qs-file-nm}
echo '{"ok":true,"id":"${http-qs-file}"}'
```

**下载**：API 命令向 stdout 输出内容，`http-output:text` 原样返回；或输出 `302:${id}` 触发重定向（见 [缓存](cache.md) 的 `cache-redirect`）。

## 注意事项

- 请求头是**大写键**（`http-header-USER-AGENT`），查询参数保留原名（`http-qs-name`）；
- 请求对象是临时的，请求结束即过期（时长由 `http-tmp-duraion`/`api-tmp-duration` 控制）；
- 拿到 `http-usr` 不等于已通过权限断言——还要配 `pvg-assert`（见 [鉴权与历史](auth-history.md)）。

## 延伸阅读

- [API 对象](api-object.md) — API 侧元数据全集
- [会话](../30.auth/session.md) — http-se-* 注入流程
- [一切皆对象](../10.concepts/02-std-obj.md)
