---
title: 3X 数据源（外部数据接入统一 IO）
module: core.mapping
keywords: [数据源, 3X, 第三方, dao, mnt, SQL, Redis, OSS, XApi, XO, 数据接入]
source:
  - core/mapping/WnIoMappingFactoryImpl.java
  - core/mapping/support/MountInfo.java
  - ext/net/xapi/XApi.java
  - ext/data/xo/impl/XoService.java
related: [io-indexer-bm, std-obj, mount-mapping, dao-mapping, xapi]
---

# 3X 数据源（外部数据接入统一 IO）

「3X」指各种各样的第三方数据源：SQL 数据库、Redis、阿里云 OSS、第三方平台的 API……
Walnut 的目标是让这些数据源**用起来与普通对象无异**——同一套 `o` 命令、同一套权限、同一套 regapi。
接入方式有三种：映射接入、数据集封装、API 网关。

## 方式一：mnt 映射（数据就在树里）

通过目录对象的 `mnt` 元数据，把整棵子树委托给外部存储（见 [IO / Indexer / BM](01-io-indexer-bm.md)）：

```bash
# 子树元数据存 SQL 表、内容存域级桶
mnt = "dao(demo/t_news)://lbm(news-data)"

# 整树挂本地静态目录
mnt = "file:///var/www/static/"

# 元数据进 Redis
mnt = "redis"
```

映射之后，对使用者完全透明：`o ~/data/news @query ...` 查的是 SQL 表，
`cat` 读的是 OSS 对象——命令不需要知道底层是什么。

常用配置约定：

- **`~/.dao/*.dao.json`** — SQL 数据源定义（连接串、账号），被 `dao(源/表)` 引用
- **`~/.io/ix/*.json`** — SQL 表映射定义（字段、索引、主键）
- **`~/.io/bm/*.json`** — Redis 桶管理器定义

详见 [mnt 映射语法全集](../20.io-storage/mount-mapping.md)与 [DAO 映射](../20.io-storage/dao-mapping.md)。

## 方式二：XO 数据集（把目录变成 REST 数据集）

`xo` 命令把任意目录封装成标准 REST 数据集（CRUD + 挂 API），内容以 sha1 签名 JSON 存储（`vxds` 桶）。
适合快速把一个目录对外发布成数据服务，不用写任何 Java。见 [xo](../40.commands/03_cmd_filter.md)。

## 方式三：XApi 网关（把第三方 API 变成命令）

外部数据不在任何存储里、只能从第三方 API 拉取时，用 `xapi`：

- 专家配置（API 清单、密钥、签名规则）放在 `~/.xapi/experts/<apiName>.expert.json`，
  账号主目录如 `~/.xapi/wechat-gh/`
- `xapi req` 生成请求、`xapi send` 直接调用，输出统一为 JSON 进管道
- 同一套配置可被命令、cron、regapi 复用，签名/缓存/密钥管理由 `XApi` 服务类兜底

详见 [xapi](../70.integrations/xapi.md)。

## 典型组合

```bash
# SQL 表里的新闻 → JSON API（无需写代码）
mount ~/data/news 'dao(demo/t_news)'

# 第三方平台的订单 → 定时拉到本地目录，再挂 API
cron add '*/10 * * * *' 'xapi send myshop orders /list > ~/data/orders.json'
```

## 深入阅读

- [IO / Indexer / BM](01-io-indexer-bm.md)：统一接口的两层实现
- [mnt 映射语法全集](../20.io-storage/mount-mapping.md)：所有映射方案
- [DAO 映射](../20.io-storage/dao-mapping.md)：SQL 数据源与表映射
- [xapi](../70.integrations/xapi.md)：第三方 API 网关
