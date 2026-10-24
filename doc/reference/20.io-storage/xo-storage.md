---
title: 对象存储集成（xo）
module: ext.xo
keywords: [xo, 对象存储, s3, cos, oss, obs, kodo, json5, 临时密钥, vofs, vxds]
source:
  - ext/xo/cmd_xo.java
  - ext/xo/impl/
  - ext/xo/builder/
  - ext/xo/util/
related: [01-io-mapping, dao-mapping, ../../../src/com/site0/walnut/ext/xo/man/zh_cn/cmd_xo.md]
---

# 对象存储集成（xo）

## 要解决的问题

各云服务厂商（AWS S3、腾讯云 COS、阿里云 OSS、华为云 OBS、七牛 Kodo）都提供对象存储服务，
功能大同小异——上传、下载、删除、列出、改元数据——但各家 SDK 和权鉴方式各不相同。
Walnut 用统一的 `xo` 命令和配置格式封装它们，并且让对象存储可以直接挂载成 IO 映射
（[vofs 索引器 / 桶](01-io-mapping.md)、[vxds 内容签名桶](01-io-mapping.md)），
对象存储里的文件用起来和普通对象无异。

## 配置布局

```bash
~/.io/                      # 所有对象存储连接配置放这里
|-- s3/
|   |-- {配置名}.json5      # 每个配置指向一个桶，一个域可配多个桶
|-- cos/
|   |-- {配置名}.json5
|-- ...                     # oss/ obs/ kodo/ 预留

~/.domain/xo_token/         # 各桶临时密钥的缓存目录
|-- s3_{配置名}             # 每个配置一份缓存，有效期看配置里的 duration
|-- cos_{配置名}
```

## 配置内容（`~/.io/{type}/{配置名}.json5`）

```js
// 腾讯云 COS 示例
{
  secretId: "AKIDQwFkQwFkQwFkQwFk",
  secretKey: "QwFkQwFkQwFkQwFkQwFkQwFkQwFkQwFk",
  bucket: "dev-local-1268933421",
  region: "ap-nanjing",
  duration: 1800,            // 临时密钥有效期（秒）
  // 访问前缀：不声明相当于桶根；声明后创建权鉴会自动设置 allowPrefixes
  prefix: "pc-sha1-22/",
  allowActions: [            // 临时密钥允许的 action 清单
    "name/cos:PutObject",
    "name/cos:PostObject",
    // 分片上传
    "name/cos:InitiateMultipartUpload",
    "name/cos:ListMultipartUploads",
    "name/cos:ListParts",
    "name/cos:UploadPart",
    "name/cos:CompleteMultipartUpload",
    "name/cos:AbortMultipartUpload"
  ]
}
```

## 调用方式

`xo` 是过滤式命令：第一个参数是 `{s3|cos}:{配置名}`，之后跟子命令：

```bash
xo s3:backup write  {本地文件} {远端路径}    # 上传对象
xo s3:backup read   {远端路径} {本地文件}    # 下载对象
xo s3:backup delete {远端路径} ...          # 删除对象
xo s3:backup list   {?远端目录}              # 列出对象
xo s3:backup get    {远端路径} -cqn         # 查看对象元数据
xo s3:backup update {远端路径} {key} {val}  # 修改对象元数据
xo s3:backup copy / move / rename ...        # 更多子命令见 man
xo s3:backup reset                           # 重置缓存的客户端实例
```

完整的子命令手册在各子命令的 man 文档里（`man xo`、`man xo write` 等），
写法见 [cmd_xo](../../../src/com/site0/walnut/ext/xo/man/zh_cn/cmd_xo.md)。

## 与 IO 映射的关系

xo 配置被两类映射共用，挂载后业务代码完全无感：

```bash
# 元数据与内容都在桶里（两段式 ID，尾段是 base64 的对象存储路径）
mnt : "vofs://s3:/home/demo#backup"

# 元数据在 Mongo，内容按 SHA1 进桶（带引用计数去重）
mnt : "://vxds(s3:sha1:22:/home/demo#backup@redis)"
```

### `vxds` 各段含义

`vxds(s3:sha1:22:/home/demo#test@redis)`：

- `s3` — 对象存储类型：`s3 | cos | oss | obs | kodo`（当前实现：s3、cos）
- `sha1` — 签名算法：对象键取内容的 SHA1（去重，天然内容寻址）
- `22` — 键的前 `N` 个字符每两位一段作为路径层级，如 `ea890abcd...` → `ea/89/0abcd...`
- `/home/demo` — 配置所在域的主目录
- `test` — 对象存储连接配置名，即 `~/.io/{type}/{name}.json5`
- `@redis` — 【可选】引用计数服务，目前仅支持 `redis`，配置文件为 `{域主目录}/.redis/{配置名}.io.refer.json`

### 引用计数安全

vxds 的内容按 SHA1 去重后，桶内物理文件靠引用计数管理生命周期（归零才删除）。
为了防止普通用户借引用计数搞乱全局 Redis，连接系统内置 Redis 时需在 web 配置中显式授权：

```properties
# 允许所有域使用系统内置 Redis
redis-host-allow-domain=*
# 或只允许指定域
redis-host-allow-domain=demo,admin
# 限允许连接的数据库（不限制则省略）
redis-database-allow=5,6
```

用户自己的 Redis 连接（与系统 `redis-host` 不一致）不受限制。

## 当前支持度

- **已实现** — `s3`（AWS）、`cos`（腾讯云）客户端
- **语法已预留** — `oss`（阿里云）、`obs`（华为云）、`kodo`（七牛），vofs/vxds 的参数校验已接受这些类型，但客户端实现仍抛 `e.vofs.UnsupportMode`

## 涉及的主要代码类

- `ext/xo/cmd_xo.java` — 命令入口（JvmFilterExecutor 模式）
- `ext/xo/impl/S3XoService.java` / `CosXoService.java` / `AbstractXoService.java` — 各家服务实现
- `ext/xo/builder/*ClientBuilder.java` — 客户端构建（配置路径、token 缓存）
- `ext/xo/util/XoClientManager.java` — 客户端实例缓存与复用

## 扩展阅读

- [mount-mapping](01-io-mapping.md) — `vofs` / `vxds` 映射语法与引用计数安全
- [cmd_xo](../../../src/com/site0/walnut/ext/xo/man/zh_cn/cmd_xo.md) — `xo` 命令手册
