---
title: 请求对象：一次请求一个对象
module: 50.regapi
keywords: [请求对象, 请求元数据, http-qs, http-header, http-cookie, http-body, args, params]
source: [ext/sys/httpapi/HttpApiModule.java]
related: [10-api-object, 30-path-params, ../10.concepts/01-std-obj.md]
---

# 请求对象概述

写 API 命令模板时，你一定会问：**`${...}` 占位符到底能写什么？**

答案完全取决于一个对象——**请求对象**。每次 HTTP 请求到来，
Walnut 都会临时创建一个对象来描述这次请求：URL、参数、请求头、Cookie
是它的元数据，请求体是它的内容。模板渲染时，占位符就是从它取值
（源码 `WnTmplX.exec(cmdPattern, apc.oReq)`）。

**它只活一小会儿**：请求结束即按 API 的 `http-tmp-duraion` 过期回收
（默认 1 分钟），所以不要长期引用它的路径。

## 它长什么样

一次请求生成的对象（在 `~/.regapi/tmp/` 下，事后可用 `o` 命令查看）：

```json5
{
  // 请求行
  http-url      : "http://host/api/demo/hello", // 完整 URL
  http-uri      : "/api/demo/hello",            // 路径部分
  http-method   : "POST",                       // 请求方法（大写）
  http-protocol : "HTTP/1.1",
  // 查询参数：每个参数一个元数据，保留原名；无值的参数为空串
  http-qs       : "name=zozoh&mime=text/plain", // 原始 Query String
  http-qs-name  : "zozoh",
  http-qs-mime  : "text/plain",
  // 请求头（大写键）与 Cookie（保留原名大小写）
  http-header-USER-AGENT : "curl/8.x",
  http-cookie-JSESSIONID : "abc...",
  // 客户端网络信息
  http-remote-addr : "127.0.0.1",
  http-remote-host : "localhost",
  http-remote-port : 5210,
  // 路径参数（见 30-path-params）
  args          : ["zozoh"],                    // 通配段数组
  params        : { uid: "zozoh" },             // 命名参数（api-param-name）
  // 域账号信息（URL /api/{usr}/... 里的账号）
  http-usr      : "demo",
  http-grp      : "demo",                       // 域账号的主域
  http-home     : "/home/demo",                 // 域账号主目录
  http-api      : "hello",                      // 本次命中的 API 对象名
  // 系统会话（请求带系统票据时注入，见 30.auth）
  http-se-ticket  : "tk...",
  http-se-me-name : "zozoh",
  http-se-me-group: "demo",
  http-se-vars    : "{...}",                    // 会话变量 JSON
  // 站点会话（API 声明 http-www-home 且票据有效时注入，见 50-auth-history）
  http-www-se-ticket : "...",
  http-www-me-id : "...", http-www-me-nm : "...",
  http-www-me : "{...}",                        // 站点用户完整元数据 JSON
  http-www-me-phone : "...", http-www-me-email : "...",
  http-www-me-mainGroup : "..."
}
```

> 除了请求本身的信息，preload 的执行结果、`http-output` 解析出的
> `output` 也会写在这个对象上，供历史记录匹配等内部机制使用
> （见 50-auth-history）。

## 在命令中读取：占位符

命令不需要知道请求对象在哪里——模板渲染时占位符自动被替换：

```bash
# API 内容
echo '{"who":"${http-qs-name}","ua":"${http-header-USER-AGENT}"}'

# 客户端
curl 'http://host/api/demo/hello?name=zozoh'
# -> {"who":"zozoh","ua":"curl/8.x"}
```

一个特别有用的占位符是 **`${id}`**——它就是请求对象自身的 ID，
`cat id:${id}` 读到的就是本次请求体。

## 请求体：对象的内容与 body 元数据

请求的 body 会**原样写入请求对象的内容**：原始报文上传（如 PUT 二进制流）
可以直接 `cp id:${id} ~/uploads/x.bin`。

声明 `http-body` 后，模块还会把 body **额外解析一份到 `body` 元数据**
（不持久化，防止太大）：

- `http-body:text` —— `body` 是原始文本字符串
- `http-body:form` —— `body` 是表单 Map，**仅支持 `a=1&b=2` 这种
  url-encoded 表单**；multipart 上传不被特殊处理，原始报文就是对象内容
- `http-body:json` —— `body` 是解析后的 JSON 对象，
  可配 `http-post-json-merge` 按条件融合字段（如自动补上当前用户 ID，见 50-auth-history）

要注意命令的沙箱环境：stdin 是空的，工作目录是域账号的主目录——
并不是请求对象本身。想在命令里进一步加工请求信息，可以借助 preload 的
`run` 把查询结果命名进上下文（见 50-auth-history）。

## 下载与重定向

- **直接输出**：命令 stdout 原样进入响应流，格式由 `http-output`、
  `http-header-*` 控制（见 10-api-object）；
- **重定向**：API 声明 `http-resp-code:302`（或 301），命令 stdout 输出
  目标 URL，服务端据此跳转（见 10-api-object）。

## 注意事项

- 请求头是**大写键**（`http-header-USER-AGENT`），查询参数与 Cookie
  保留原名大小写（`http-qs-name`、`http-cookie-JSESSIONID`）；
- 请求对象是临时的，请求结束即过期，不要长期引用它的路径；
- 命令沙箱的 stdin 为空、工作目录是域账号主目录；请求对象在
  `~/.regapi/tmp/` 下按请求生成，主要供模板渲染与模块内部机制使用；
- 拿到 `http-usr` 只是知道了域账号，拿到 `http-www-me-*` 只是恢复了站点会话——
  都不等于通过了动作级权限检查，那需要配 `pvg-assert`（见 50-auth-history）。

# 延伸阅读

- 10-api-object — API 侧元数据全集
- 30-path-params — args / params 的来历
- 30.auth — http-se-* 的会话背景
- 50-auth-history — preload 与历史记录如何使用请求对象
- 一切皆对象 — 10.concepts/01-std-obj.md
