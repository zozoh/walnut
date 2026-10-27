---
title: API 对象：一个文件一个接口
module: 50.regapi
keywords: [regapi, api 对象, http-api, api-access-token, http-output, http-header, http-dynamic-header, http-cross-origin, cache-test, http-www-home, preload, pvg-assert]
source: [ext/sys/httpapi/HttpApiModule.java]
related: [20-request-object, 30-path-params, 40-cache, 50-auth-history, ../00.start-here/first-api.md]
---

# API 对象概述

如果想让 `Walnut` 快速的向外部提供一个 HTTP API 接口，我们能想到的最快的办法就是

**直接建立一个对象**

我们管这个描述一个接口的对象叫做 **API 对象**。它所遵守的唯一约定就是:

**需要放在 `~/.regapi/api/` 目录下**

譬如:

```bash
~/.regapi/api/
|-- hello     # 这是一个普通的文件对象
              # 它的内容也非常简单，就是一个命令模板

$ cat ~/.regapi/api/hello
echo 'hello world'
```

假设这个域的账号是 `demo`，主机是 `http://localhost:8080`，
现在用 Linux 自带的 curl 就能调用它：

```bash
curl 'http://localhost:8080/api/demo/hello'
# -> hello world
```

如果我们想把这个接口改成可以接受动态参数，也非常简单，直接改变 `hello` 对象的内容：

```bash
$ cat ~/.regapi/api/hello
echo 'hello ${http-qs-name}'
```

> 这里的 `${http-qs-name}` 就是一个固定占位符，本文后面章节会详细讲解

再调用一次：

```bash
curl 'http://localhost:8080/api/demo/hello?name=everybody'
# -> hello everybody
```

HTTP 的 query string 参数，会变成一个固定上下文变量 `${http-qs-name}`，最后
这个接口相当于执行了命令 `echo 'hello everybody'`

当然，`${http-qs-name}` 只是冰山一角——每次请求，Walnut 都会临时创建一个
**请求对象**：Query String、Header、Cookie 是它的元数据，请求体是它的内容，
命令模板里的 `${...}` 占位符就是从它取值（详见 20-request-object）。

看懂这个例子后，你马上会问：**这个 API 对象到底支持哪些属性？**
下一节先给一张全集速览，然后逐条细讲。

## 属性总览

API 对象的全部行为都通过元数据配置。下面这个块把几乎所有元数据按组列了一遍，
注释即语义，先建立整体印象，各属性的细节在「属性详解」里分条展开：

```json5
{
  //=========================== 访问控制
  // 访问令牌：客户端须在请求参数里带 "at=xxx"（值与令牌文件内容一致），否则 403
  // = 后面的参数名可选，默认 "at"；令牌值默认读 ~/.domain/api_access_token
  api-access-token: "http-qs-at=~/.domain/api_access_token",
  // 站点登录（谁）：按票据恢复站点会话，注入 http-www-me-* 元数据
  http-www-home: "~/www",              // 站点目录（支持 explain 表达式）
  http-www-ticket: "http-qs-ticket",   // 从哪里取票据
  http-www-auth: true,                 // true 则必须已登录，否则 403
  // 业务权限（能不能做）：角色×动作断言，项内 OR、项间 AND
  pvg-setup: "~/www/pvg.json",         // 权限表路径（也可用站点目录的 pvg_setup 元数据）
  pvg-assert: ["order:del|order:admin", "order:view"],

  //=========================== 参数处理
  // 默认 true：仅对 Query String 参数（http-qs-*）做防注入过滤
  // （去单双引号、反引号、回车、分号；路径参数、Header、Cookie、body 不过滤）
  http-safe-params: true,
  // 把请求体解析进 body 元数据："text" | "form" | "json"（json 可按条件融合字段）
  http-body: "json",
  // 按条件把 JSON body 的字段融合进请求体（详见 50-auth-history 的执行前干预）
  http-post-json-merge: [{test:{ts:"^(comments)$"}, update:{uid:"=http-www-me-id"}}],
  copy-cookie: ["SEID", "ABC"],        // 【选】把这些 Cookie 复制进线程上下文

  //=========================== 响应控制
  // 输出类型："text" | "json"，两者都原样回传 stdout；
  // json 额外把 stdout 解析进请求对象的 output 元数据（供历史记录用）
  http-output: "json",
  // 固定写入响应头（名称大小写不敏感，输出时转大写；值支持 ${xxx} 占位）
  "http-header-Content-Type": "text/plain; charset=utf-8",
  // 动态响应头：命令先输出到缓冲，按 "HTTP/1.1 200 OK" 文本格式解析头部（见 httpout 命令）
  http-dynamic-header: false,
  http-resp-code: 200,                 // 【选】预设响应码；301/302 时 stdout 即跳转 URL
  http-resp-buff-size: 1024,           // 【选】响应缓冲大小（Jetty 默认 32K）
  // 跨域快捷开关：等价于一组 Access-Control-Allow-* 头；"*" 自动用当前 Origin 回填
  http-cross-origin: "*",

  //=========================== 执行环境
  // 为 true 时本次执行带钩子（等价写入动作，触发对象钩子）
  run-with-hook: false,
  // 临时请求对象的过期毫秒（默认 1 分钟）。注意：这是历史拼写，少一个 t
  http-tmp-duraion: 60000,

  //=========================== 执行前预加载 / 执行后历史记录（详见 50-auth-history）
  preload: [{test: {...}, run: {...}}],   // 执行前先跑一组命令，结果并入上下文
  histest: {...},                      // 判断本次请求是否记录历史
  history: [{uid:"=http-www-me-id", opt:"save"}],  // 历史记录模板（可多条）
  hismetas: [{test:{...}, update:{...}}],  // 按条件融合历史字段
  hisname: "_history",                 // 历史记录对象键名

  //=========================== 缓存（详见 40-cache）
  cache-test: [{match:{...}, path:"~/.cache/api/get-${http-qs-id}.json"}],
  cache-duration: 60,                  // 缓存有效期（秒），默认永久
  cache-finger-key: "http-query-finger",  // 指纹校验键（默认不校验）
  cache-lazy: true,                    // 命中后自动生成缓存
  cache-redirect: "http://cdn.xx/${sha1Path}"  // 声明则以 302 跳转到 CDN
}
```

## 属性详解

### 访问控制

#### `api-access-token`：访问令牌

- 类型：string；格式 `请求键=令牌文件路径`，两半都可选
  - 不带 `=`：整个值是请求键，令牌文件默认 `~/.domain/api_access_token`
  - 带 `=`：等号左边是请求键，右边是令牌文件路径（支持 `~`）
- 行为：从请求元数据取请求键对应的值（请求键一般是 `http-qs-at`，
  即查询参数 `at`），与令牌文件内容（去除首尾空白）做**完全一致**比较；
  客户端没带、带错、令牌文件不存在，一律 403
- 取值示例：`"http-qs-at=~/.domain/api_access_token"`、`"at"`（用默认文件）
- 典型场景：给内部系统开接口，令牌文件由域管理员控制，改文件即换密钥

#### `http-www-home` / `http-www-ticket` / `http-www-auth`：站点会话

- `http-www-home`：站点目录或站点 JSON 配置文件的路径；
  支持 explain 模板——域名转发场景可写 `"=wn_www_site?~/www/demo"`
  （`wn_www_site` 由全局过滤器注入请求属性，表示当前域名对应的站点路径）
- `http-www-ticket`：从哪个请求键读票据，默认 `http-qs-ticket`
- `http-www-auth`：是否必须已登录；默认跟随 `http-www-home`
  （声明了 home 即要求登录）；无有效会话 → 403
- 票据有效时注入 `http-www-me-*` 一组元数据（见 20-request-object）
- 细节与示例见 50-auth-history

#### `pvg-setup` / `pvg-assert`：业务权限断言

- `pvg-assert`：字符串数组，每项是一组 `动作|动作`（项内是 OR，项间是 AND），
  当前站点角色任意命中一组才放行，否则 403
- `pvg-setup`：权限表对象路径；不设置时回落到站点 auth 选项里的 `pvg_setup`
- ⚠️ 当前源码守卫疑似写反（`HttpApiModule.__do_www_check_pvg`）：
  正常配置站点时断言被跳过、实际不生效，修复前不要依赖它做权限控制
- 细节见 50-auth-history

### 参数处理

#### `http-safe-params`：查询参数防注入

- 类型：bool；默认：`true`
- 行为：对请求元数据里所有 `http-qs-*` 的字符串值做过滤，
  去除 `` ` ' " ``、回车换行、分号（源码 `WnStr.safeTrim`）
- 不过滤：路径参数（`args`/`params`）、请求头、Cookie、请求体——
  body 的清洗交给 `jsonx`/`httpparam` 等命令，那是它们的本职

#### `http-body`：请求体解析

- 类型：string；取值：`text` | `form` | `json`；缺省：不解析
- 行为：无论是否声明，body 总是**原样写入请求对象的内容**——
  模板里 `${id}` 就是请求对象自身的 ID，`cat id:${id}` 读到的就是请求体；
  声明本属性后，还会**额外**把 body 解析一份到请求对象的 `body` 元数据
  （不持久化，防止太大）：
  - `text` → `body` 是原始文本字符串
  - `form` → `body` 是表单 Map，**仅支持 `a=1&b=2` 这种 url-encoded 表单**；
    multipart 上传不被特殊处理，原始报文就是对象内容
  - `json` → `body` 是解析后的 JSON 对象（可配 `http-post-json-merge` 融合字段）
- 注意：声明了 `http-dynamic-header` 或 301/302 重定向时，本属性被无视

#### `http-post-json-merge`：融合字段进 JSON 请求体

- 类型：对象数组；生效前提：`http-body:"json"`
- 行为：逐项遍历，第一个 `test` 命中的项生效（AutoMatch 语法，对请求元数据匹配；
  `test` 缺省总匹配，空 `update` 表示跳过）；
  `update` 经 explain 渲染（可引用 `${http-www-me-id}` 等上下文）后，
  与解析出的 JSON body **深层融合**（仅当 body 是对象），融合结果重新写回请求对象内容
- 典型用途：自动补 `uid`、`ct` 等服务端字段——客户端不必传，也传不进来
- 示例：

```js
http-post-json-merge : [{
  test   : { "ts" : "^(comments|profile)$" },
  update : { "uid" : "=http-www-me-id" }
}]
```

#### `copy-cookie`：Cookie 复制进线程上下文

- 类型：字符串数组
- 行为：把列出的 Cookie 从请求复制进 `WnContext`：名为 `SEID` 的 Cookie
  会成为当前线程的会话票据，其余以原名存入上下文变量，命令执行期间可读
- 一般用不到——站点票据有专门的 `http-www-*` 机制

### 响应控制

#### `http-output`：记录命令输出

- 类型：string；取值：`text` | `json`；缺省：不记录
- 行为：响应本身总是 stdout 原样流，两种取值都不改变回传内容；
  声明后 stdout 会**额外**记入请求对象的 `output` 元数据
  （`text` 原样 / `json` 解析为对象），供 `histest`、`hismetas`、`history`
  模板引用（如 `${output.title}`）
- 注意拼写是 `http-output`（旧文档里出现过 `http-ouput` 笔误）；
  与 `http-dynamic-header`、301/302 重定向同时声明时会被无视

#### `http-header-*`：固定响应头

- 前缀元数据：键名大小写不敏感，输出时统一转大写
- 值是字符串模板，渲染上下文为**请求对象的元数据**
  （可引用 `${http-qs-*}`、`${params.*}` 等）
- 两个特殊键：
  - `http-header-CONTENT-TYPE` —— 决定响应的 Content-Type
  - `http-header-CONTENT-DISPOSITION` —— 支持 `attachment; filename="xx"` 形式，
    会按客户端 UA 编码文件名，用于下载响应
- 其余键原样写入响应头；优先级高于 `http-cross-origin` 的自动跨域头

#### `http-dynamic-header`：动态响应头

- 类型：bool；默认：`false`
- 行为：开启后 stdout 不再直接作为响应体，而是按
  `HTTP/1.1 200 OK\r\nHeader: v\r\n\r\n<body>` 的文本格式解析：
  空行前的头部写回响应，空行之后的内容才是响应体
- 适合 ETag / 302 / Range 协商等多媒体场景，配合 `httpout` 命令：

```bash
httpout -body ${http-qs} \
  -etag  '${http-header-IF-NONE-MATCH?none}' \
  -range '${http-header-RANGE?}'
```

- 副作用：缓存被无视，`http-output` 的记录也被无视

#### `http-resp-code`：响应码

- 类型：int；默认：`200`
- `301`/`302` → 进入重定向模式：命令 stdout 必须是目标 URL，
  服务端据此跳转（stderr 非空则返回 500）；其他值作为普通响应状态码

#### `http-resp-buff-size`：响应缓冲

- 类型：int（字节）；大于 0 才生效
- Web 容器默认有响应缓冲（Jetty 是 32K），缓冲内的响应会自动带
  Content-Length；需要更大的缓冲时用这个属性调整

#### `http-cross-origin`：跨域快捷开关

- 类型：string；常用值：`*` 或具体 Origin（如 `https://foo.com`）
- 行为：等价于自动输出一组 `Access-Control-Allow-*` 响应头
  （Origin / Methods / Headers / Credentials）；请求带 `Origin` 头时，
  `*` 会自动回填为当前 Origin
- 只作用于带 Origin 的跨域请求；OPTIONS 预检由模块直接应答
- 用 `http-header-Access-Control-*` 单独指定优先级更高

### 执行环境

#### `run-with-hook`：带钩子执行

- 类型：bool；默认：`false`
- 本次执行等同一写入动作，会触发对象钩子

#### `http-tmp-duraion`：请求对象存活时长

- 类型：long（毫秒）
- 请求结束后为请求对象设置 `expi` 过期时间；小于 1000ms 则立即删除
- 默认值链：本元数据 > 站点对象的 `api_req_du`（秒）>
  全局配置 `http-api-tmp-duration`（默认 60000，即 1 分钟）
- ⚠️ 历史拼写 `duraion`（少一个 t），写配置时照抄

### 预加载与历史记录

#### `preload`：执行前预加载

- 执行 API 命令之前，先按条件跑一组命令，把结果并入请求元数据，供模板引用
- 结构是 `{test, appends, run}` 的列表，逐项匹配、先中先得：
  `appends` 先渲染并入元数据，`run` 是「结果键 → 命令模板」的映射——
  键 `...` 表示结果整体并入，键前缀 `!` 表示结果不做 JSON 解析
- 细节与示例见 50-auth-history

#### `history` / `histest` / `hisname` / `hismetas`：历史记录

- `histest`：AutoMatch 条件（对请求对象匹配），决定本次请求是否记录历史
- `history`：历史记录对象模板（explain 语法，如 `uid:"=http-www-me-id"`），可多条
- `hisname`：历史配置名，默认 `_history`，对应 `~/.domain/history/<hisname>.json`
- `hismetas`：按条件与 `history` 模板融合，补充记录字段
- ⚠️ 当前 `_record_history` 的写库代码整体被注释（TODO），
  实际只检查配置文件、缺失时打警告
- 细节见 50-auth-history

### 缓存

#### `cache-test`：缓存匹配

- 类型：NutMap 数组，逐项尝试、先中先得；每项 `{match, path, mime}`
- `match` 只对**查询参数**做匹配（键写参数原名，如 `day`），缺省总匹配
- `path` 是缓存文件路径模板，渲染上下文为请求元数据（可用 `${http-qs-*}`、`${params.*}`）；
  `mime` 是命中时的 Content-Type
- 细节见 40-cache

#### `cache-duration`：缓存有效期

- 类型：int（秒）；`0` 或缺省 = 永久有效
- 按缓存文件的最后修改时间判定：太老即视为失效，重新执行命令

#### `cache-finger-key` / `cache-finger-as`：请求指纹

- 对原始查询串计算摘要（默认算法 MD5），命中时比对缓存对象上该键的值——
  把参数变化纳入缓存校验，防止同路径不同参数的串数据
- `cache-finger-as` 可换成 SHA1 等其他摘要算法

#### `cache-lazy`：懒加载

- 类型：bool；默认：`true`
- 未命中时正常执行命令，stdout 一边响应一边写入缓存路径，下一次请求即命中

#### `cache-redirect`：缓存重定向

- 类型：string（URL 模板）
- 命中时不输出缓存内容，而是 302 跳转到渲染后的 URL；
  模板上下文是缓存对象的元数据，另提供 `${sha1Path}` 占位符
  （sha1 前 4 位 + `/` + 其余部分的路径形式），配合 CDN 做内容寻址缓存

## 场景速查

- **公开接口**：什么都不用配，放入 `~/.regapi/api/` 即可被执行；
- **内部接口**：`api-access-token`，客户端带 `?at=xxx`；
- **JSON 接口**：命令向 stdout 输出 JSON 文本即可；要记录输出供历史模板引用，加 `http-output:json`；
- **CORS**：`http-cross-origin:*`；
- **302 跳转**：`http-resp-code:302`，命令 stdout 输出目标 URL；
- **文件下载**：`http-header-CONTENT-DISPOSITION:'attachment; filename="a.zip"'`，内容走 stdout；
- **POST 表单**：`http-body:form`，表单字段解析进请求对象的 `body` 元数据（见 20-request-object）；
- **读请求体**：模板里 `cat id:${id}`——`${id}` 就是请求对象自身的 ID。

## 注意事项

- `http-api` 不是 API 对象的配置项：它是**请求对象**上的元数据（`__fill_req_meta`
  注入，值为本次命中的 API 对象名，见 20-request-object）；旧文档所谓
  「声明 `http-api:true`」并无对应实现；
- 服务端不校验可执行位：`~/.regapi/api/` 树下被路由命中的对象都会被执行；
- `http-method` 元数据在服务端**没有对应的校验逻辑**（源码只把请求方法记录进
  请求对象元数据），不能用来限定请求方法；
- 在 API 对象上预设 `http-qs-*` 参数（想当默认值用）目前**不会**被服务端合并进
  请求元数据，占位符取不到；参数默认值请用 preload 的 `appends` 实现；
- 响应头名按惯例大写：`http-header-CONTENT-TYPE`、`http-header-ACCESS-CONTROL-*`；
- `http-tmp-duraion` 是历史上的拼写（少一个 t），写配置时照抄现有拼写；
- 元数据全集的权威出处是 `HttpApiModule.java`，冲突时以源码为准。

# 延伸阅读

- 20-request-object — 临时请求对象：请求侧能拿到什么
- 30-path-params — 路径参数与 `_ANY` 通配
- 40-cache — cache-* 缓存策略
- 50-auth-history — 站点登录、权限断言与历史记录
- 第一个 API — 从零跑通一个接口
