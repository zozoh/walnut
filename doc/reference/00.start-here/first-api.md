---
title: 第一个 HTTP 接口
module: ext.sys.httpapi
keywords: [regapi, HTTP接口, 快速上手, curl, API, hello world]
source:
  - ext/sys/httpapi/HttpApiModule.java
  - ext/sys/httpapi/HttpApis.java
related: [quick-start, api-object, request-object]
---

# 第一个 HTTP 接口

Walnut 的 HTTP 接口不需要写 Java：**一个文件就是一个 API**。下面用 root 账户在 3 分钟内调通第一个接口。

## 步骤 1：创建 API 文件

以 root 登录系统会话后（`login root`，或在域 Shell 中直接执行）：

```bash
# 创建 API 目录与文件：~ 即 /home/root
mkdir ~/.regapi/api -p

# 文件内容是一行命令：echo 输出即 HTTP 响应体
echo 'echo "hello ${http-qs-name?world}"' > ~/.regapi/api/hello

cat ~/.regapi/api/hello
# → echo "hello ${http-qs-name?world}"
```

也可以不登录，在域 Shell / 管理界面里直接创建文件，效果相同。

## 步骤 2：调用

```bash
curl 'http://localhost:8080/api/root/hello'
# → hello world

curl 'http://localhost:8080/api/root/hello?name=walnut'
# → hello walnut
```

URL 规则：`/api/{用户名}/{API路径}`。本例 `usr=root`、`api=hello`。

## 发生了什么

`HttpApiModule`（`@At("/api")`）处理请求：

1. 找到 `root` 用户，定位其主目录下的 API 文件 `~/.regapi/api/hello`；不存在则 **404**。
2. 把当前线程切换为 `root` 身份（API 以文件所有者的权限执行）。
3. 生成**请求对象**（临时文件，存在 `~/.regapi/tmp/`，默认缓存 1 分钟）：
   - Query String `name=walnut` → 元数据 `http-qs-name`
   - 请求头 → `http-header-*`，Cookie → `http-cookie-*`
   - 请求体 → 文件内容
4. 以请求对象为上下文**渲染命令模板**：`${http-qs-name?world}` 被替换为 `walnut`（未传则用默认值 `world`）。
5. 执行命令，`stdout` 写入 HTTP 响应（默认 `text/plain; charset=utf-8`）。

## 步骤 3：返回 JSON 并自定义响应头

```bash
# 元数据声明响应头
o ~/.regapi/api/hello -u 'http-header-Content-Type:"application/json"'
# 命令内容：echo 输出 JSON 文本，经 json -c 格式化（压缩成单行）
echo 'echo "{msg:\'hello ${http-qs-name?world}\'}" | json -c' > ~/.regapi/api/hello
```

```bash
curl 'http://localhost:8080/api/root/hello?name=walnut'
# → {"msg":"hello walnut"}   （Content-Type: application/json）
```

> 这里用 `o -u` 修改元数据、`json` 生成 JSON，都是 Walnut 命令，语法详见
> [命令参考：文件对象组](../40.commands/file-obj.md)。

## 常用套路

| 想要 | 做法 |
|------|------|
| 返回对象内容 | `o id:${http-qs-id} -cqn` 或 `cat id:${id}` |
| 返回文件 | `cat ~/data/report.pdf` + 元数据 `http-header-Content-Type:application/pdf` |
| 接收 POST body | `cat id:${id}` 即请求体流，直接转存：`cat id:${id} > ~/uploads/a.bin` |
| 动态响应头/302 | 元数据 `http-dynamic-header:true`，命令输出 `HTTP/1.1 302 ...` 头部 |
| 跨域 | 元数据 `http-cross-origin:"*"` |
| 防注入 | 默认开启（`http-safe-params`），Query String 会过滤引号/分号 |

## 下一步

- [api-object](../50.regapi/api-object.md)：API 对象全部元数据与行为定制
- [request-object](../50.regapi/request-object.md)：请求对象元数据全集
