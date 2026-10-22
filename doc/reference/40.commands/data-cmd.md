---
title: 数据处理命令组
module: ext.data
keywords: [sqlx, entity, like, favor, score, history, xo, val, seq, fake, 数据]
source:
  - ext/data/sqlx/
  - ext/data/entity/
  - ext/xo/
  - val/
related: [dao-mapping, mount-mapping, query-agg, biz-privilege]
---

# 数据处理命令组

在对象/映射之上做业务数据操作：自由 SQL、通用实体（赞/收藏/打分/历史）、XO 数据集、值生成器、假数据。

## sqlx：自由 SQL 模板

SQL 以**模板文件**形式放域内（`man sqlx`）：

```bash
~/.sqlx/pet.sql
```

```sql
-- @name=query
-- @type=select
SELECT * FROM t_pet WHERE ${@vars=where};
-- @name=update
UPDATE t_pet SET${@vars=update} WHERE id=?;
```

调用：

```bash
sqlx pet query -vars 'nm:"旺财",kind:"dog"' -cqn
sqlx pet update -vars 'id:"x1",age:3'
sqlx pet query -pipe 'wc -l'        # 结果接管道
sqlx pet exec 'DELETE FROM t_pet WHERE age>20'
```

特性：多语句模板（`@name` 分段）、`${@vars}` 从上下文拼 where/set 片段、`-pipe` 管道、JSON 输出（`-json`）、事务（`sqlx trans`，见 man）。数据源来自 `~/.dao/*.dao.json`（见 [dao-mapping](../20.io-storage/dao-mapping.md)）。

## entity：通用实体

高频轻量数据，按类型选存储（`man like/favor/score/history`）：

- **`like`** — 存储：Redis Set — 场景：点赞：yes/no/all/count/isLike
- **`favor`** — 存储：Redis ZSet(时间分) — 场景：收藏：含收藏时间排序
- **`score`** — 存储：Redis ZSet — 场景：打分：一人一次，sum/resum
- **`history`** — 存储：SQL 表 — 场景：操作历史（结构见通用实体文档）

```bash
like yes target_1 $UID            # 点赞
like count target_1               # 多少人赞了
favor yes $UID target_1 %ms:now   # 收藏
score it target_1 $UID 85         # 打分
history add ~/myapp -vars 'uid:"u1",tid:"t1",opt:"save"'   # 历史埋点
```

## xo：对象数据集框架

把一类对象封装成标准 CRUD 数据集并挂到 HTTP API（`man xo`）：

```bash
xo ~/datasets/orders @list
xo ~/datasets/orders/4k2.. @get -cqn
xo ~/datasets/orders/4k2.. @update 'status:"OK"'
```

适合做「域名/datasets」形式的 REST 数据接口后端。

## val / seq / uuid / random：值生成器

```bash
uuid                    # UUID32
random -s 6             # 6位随机串
seq create ~/seq/order  # 建序列对象
seq next ~/seq/order    # 取下一个值（原子）
datex fmt %ms:now+1d 'yyyy-MM-dd'   # 日期表达式
```

ID 生成器（`val/`）：UU32、Snowflake、日期序列等，Java 侧 `ValueMakers`/`WnIds`。

## fake：假数据生成

造测试数据（`man fake`）：

```bash
fake name              # 人名
fake bean '{nm:"@name",age:"@integer(1,100)"}' 10 -cqn   # 10 条假记录
fake text 200          # 一段文字
fake sentence          # 一句话
```

## 场景速查

- **给已有 MySQL 库做查询接口**：`~/.dao` 配源 + sqlx 模板 + regapi 包装
- **内容平台点赞收藏打分**：entity 三件套（Redis），零建表
- **需要原子发号**：`seq` 对象
- **压测/演示数据**：`fake` + `run` 批量

## 注意事项

- entity 的 Redis 前缀可在域配置中改（`setup.prefix`），多站点共库时用前缀隔离。
- sqlx 模板变化即时生效；注意 SQL 注入——`-vars` 值经参数绑定，勿直接拼 `${}`。
- `like/favor/score` 的「目标 ID」建议用对象 id 或业务唯一键，含义由调用方约定。
