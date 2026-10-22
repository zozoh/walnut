---
title: API 对象：一个文件一个接口
module: 50.regapi
keywords: [regapi, api 对象, http-api, api-access-token, api-param-name, preload, run-with-hook]
source: [ext/sys/httpapi/]
related: [request-object, path-params, cache, auth-history, ../00.start-here/first-api.md]
---

# API 对象：一个文件一个接口

在 Walnut 里，**一个 API 就是一个文件**（HTTP API，也叫 regapi）。请求路径映射到虚拟文件系统，文件的内容（命令或 JS）就是接口实现，文件的元数据决定鉴权、缓存、输出等行为。

处理核心在 `src/com/site0/walnut/ext/sys/httpapi/HttpApiModule.java`（1257 行，所有元数据的权威出处）。

## 最小例子

```bash
# 建一个 API
echo 'echo "hello ${http-qs-name}"' > /home/demo/api/hello
chmod +x /home/demo/api/hello
o /home/demo/api/hello -e 'http-api:true'

# 调用
curl 'http://host/api/hello?name=zozoh'
# -> hello zozoh
```

任何设置了 `http-api:true` 元数据、且可执行（`+x`）的对象，都会成为 HTTP 接口。

## 执行模型

1. 请求路径 → 定位 API 对象（支持 `_ANY` 通配，见 [路径参数](path-params.md)）；
2. 组装 **API 上下文**（`apc`）：请求对象（http-api 临时对象）+ 元数据 NutMap；
3. 依次执行各阶段：鉴权 → 会话/权限 → preload → 执行命令 → 输出 → 历史记录；
4. 输出响应（`http-output` 决定 text/json）。

API 对象的执行内容可以是命令（默认），也可以用 `http-body`/`http-output` 声明输入输出类型（见 [请求对象](request-object.md)）。

## API 对象元数据全集

在 API 对象（或 `/etc/api` 站点配置目录的对象）上设置：

### 访问控制

- **`http-api:true`**：声明本对象是 API
- **`api-access-token`**：访问令牌名——客户端必须在请求参数里带同名 key 的正确 token，否则 `e.api.auth` 拒绝
- **`pvg-setup` / `pvg-assert`**：站点权限：设置上下文角色、断言动作权限（见 [鉴权与历史](auth-history.md)）

### 请求解析

- **`api-param-name`**：路径参数注入名（配合 `_ANY`，见 [路径参数](path-params.md)）
- **`http-method`**：限定请求方法（GET/POST...），不匹配报 404/405
- **`http-body`**：请求体类型：`text` / `form` / `json`
- **`http-post-json-merge`**：为 true 时把 JSON body 的字段合并进参数表
- **`http-qs-*`**：在 API 对象上预设参数（`http-qs-mime`、`http-qs-ticket` 等）
- **`http-safe-params`**：安全参数名列表，防注入

### 响应控制

- **`http-output`**：输出类型：`text`（原始流）/ `json`（格式化 JSON 响应）
- **`http-header-<NAME>`**：预设响应头；值支持 `${key}` 占位（从请求参数取值），如 `http-header-X-FOO:${name}`
- **`http-dynamic-header`**：动态头开关——配合模板动态生成响应头
- **`http-resp-code`**：预设响应码
- **`http-resp-buff-size`**：响应缓冲大小
- **`http-cross-origin`**：跨域许可（`Access-Control-Allow-Origin`）
- **`copy-cookie`**：从请求复制哪些 Cookie 到响应

### 执行环境

- **`run-with-hook`**：为 true 时执行前先触发钩子（等同写入动作）
- **`http-tmp-duraion` / `api-tmp-duration`**：API 临时对象（http-api 对象）的过期时长
- **`http-home`**：API 工作目录（默认 API 所在目录）
- **`http-protocol`**：协议约束（http/https）

### 预加载与记录

- **`preload`**：执行前预加载数据到上下文（数组：`test`/`appends`/`run`），见 [鉴权与历史](auth-history.md)
- **`history`**：为 true 时写历史记录对象（默认名 `_history`）
- **`hisname`**：历史记录对象的键名（默认 `_history`）
- **`hismetas`**：历史记录要保存的元数据白名单

## 场景速查

- **公开接口**：只加 `http-api:true`；
- **内部接口**：`api-access-token`，客户端带 `?token=xxx`；
- **JSON 接口**：`http-output:json`，命令最后向 stdout 输出 JSON 文本；
- **CORS**：`http-cross-origin:*`；
- **文件上传**：`http-body:form`，然后命令里 `cp id:${id} ~/uploads/`（见 [请求对象](request-object.md)）。

## 注意事项

- API 文件必须可执行（`chmod +x`），否则按静态文件返回；
- 响应头名按惯例大写：`http-header-CONTENT-TYPE`、`http-header-ACCESS-CONTROL-*`；
- 元数据拼写以源码为准——注意 `http-tmp-duraion` 是历史上的拼写（少一个 t），写配置时照抄现有拼写。

## 延伸阅读

- [请求对象](request-object.md) — 请求侧元数据全集
- [路径参数](path-params.md) — _ANY 与动态路由
- [缓存](cache.md) — cache-* 元数据
- [第一个 API](../00.start-here/first-api.md)
