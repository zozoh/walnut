---
title: 桶管理器 - vxds
module: core.bm.bmv
keywords: [桶管理器, vxds, VXDataSignBM, 对象存储, 内容签名, SHA1, 引用计数, vofs, xo]
source:
  - core/mapping/bm/VXDataSignBMFactory.java
  - core/mapping/support/WnVoBMOptions.java
  - core/bm/bmv/VXDataSignBM.java
related: [01-io-mapping, xo-storage, 41-indexer-vofs]
---

# 桶管理器 - vxds

内容存到**对象存储**（S3/COS），但不像 `vofs` 那样保留原始目录结构——
`vxds` 按**内容的 SHA1 签名**存桶：同一内容无论被多少对象引用，桶里只有一份。
这是对象存储版的「sha1 桶」，天生为海量小中文件的去重存储设计。

## mnt 语法

```bash
mnt : "://vxds({s3|cos|oss|obs|kodo}:sha1:{段数}:{域主目录}#{配置名}[@redis])"
```

```bash
mnt : "://vxds(s3:sha1:22:/home/demo#attachment@redis)"
```

（索引管理器省略，元数据仍走全局 Mongo；组合 `sql(...)::vxds(...)` 亦可。）

## 配置参数

- `{s3|cos|oss|obs|kodo}` — 对象存储类型（当前实现：s3、cos；其余参数校验已预留）
- `sha1` — 签名算法：对象键取内容的 SHA1
- `{段数}` — 键的前 N 个字符每两位一段作为路径层级，如 `ea890abcd...` → `ea/89/0abcd...`
  （段数取值 1~9；这是为了让一个目录下的文件数别太多）
- `{域主目录}` — 配置所在域的主目录（如 `/home/demo`）
- `{配置名}` — 对象存储连接配置，存放在 `{域主目录}/.io/{type}/{配置名}.json5`，
  格式与 `xo` 命令的配置完全一致，见 [对象存储集成](xo-storage.md)
- `@redis` — 【可选】引用计数服务，目前仅支持 `redis`，
  配置文件为 `{域主目录}/.redis/{配置名}.io.refer.json`

## 引用计数与安全

内容按 SHA1 去重后，桶内物理文件靠引用计数管理生命周期（归零才删除）。
为了防止普通用户借引用计数搞乱全局 Redis，连接**系统内置 Redis** 时需在
`conf/web.properties` 显式授权：

```properties
redis-host-allow-domain=*          # 允许所有域
redis-host-allow-domain=demo,admin # 或只允许指定域
redis-database-allow=5,6           # 限允许连接的数据库（可省略）
```

用户自己的 Redis（与系统 `redis-host` 不一致）不受限制。

## 注意事项

- 对象键是内容签名，**没有目录层级**——遍历时按段数前缀列举，语义与 vofs 不同
- 想保留对象存储里的目录结构（元数据也在桶里），见 [对象存储映射](41-indexer-vofs.md)

## 扩展阅读

- [对象存储集成](xo-storage.md) — json5 连接配置、xo 命令
- [对象存储映射](41-indexer-vofs.md) — vofs：元数据与内容都在桶里
