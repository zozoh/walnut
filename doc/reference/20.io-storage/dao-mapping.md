---
title: .dao 数据源与 SQL 表映射
module: core.mapping
keywords: [dao, 数据源, SQL映射, 表映射, 字段映射, 分表, 自动建表]
source:
  - core/indexer/sql/
  - core/mapping/indexer/
related: [mount-mapping, sqlx, io-model]
---

# .dao 数据源与 SQL 表映射

把目录映射到一张 SQL 表：元数据进关系库（支持事务/联查/海量），内容仍走桶。需要两个配置文件 + 一个 `mnt`。

## 第一步：定义数据源 `~/.dao/{name}.dao.json`

```js
{
  "url" : "jdbc:mysql://127.0.0.1:3306/walnut",
  "username" : "root",
  "password" : "root",
  "maxActive": 50,
  "maxWait" : 15000,
  "testWhileIdle" : true
}
```

默认数据源名 `default`。

## 第二步：定义表映射 `~/.io/ix/{name}.json`

```js
{
  "dao": "demo",                // 数据源名
  "tableName": "t_abc",           // 表名；分表可用 t_abc_${id} 动态表名
  "autoCreate" : true,            // 表不存在时自动建表（false 则抛错）
  //------------------------------------------------
  // 字段映射：WnObj 标准字段 ↔ 表列
  // 内置字段(id|pid|nm|race|c|m|g|mnt|ln|tp|mime|sha1|len|d0|d1|lbls|ct|lm|st|expi)
  // 按需声明；特殊字段 {name:".."} 对应 metas 列，多余字段自动收纳/解开
  "fields": [{
    "name": "id",                 // WnObj 标准字段名
    "type": "String",             // String|Integer|Long|Float|Double|Boolean|Object|SArray|List|JSON
    "columnName": "id",           // 表列名（默认同 name）
    "columnType": "VARCHAR",      // 建表类型（AUTO 自动）
    "width": 26,
    "notNull": true,
    "autoIncreasement": false,
    "defaultValue": "xxxx",        // 支持 ${key} 占位符
    "isVersion": false,            // 乐观锁版本字段
    "insert": true, "update": true
  }],
  "pks": ["id"],                  // 主键
  "objKeys": ["id","pid","nm", "race"],  // 映射必须具备的 WnObj 字段
  "indexes": [{                   // 首次建表自动创建
    "unique": false,
    "name": "IX_PID_NM",
    "fields": ["pid","nm"]
  }]
}
```

## 第三步：挂映射

```bash
mount ~/biz/orders 'dao(demo/t_abc)://lbm(abc-data)'
# 之后 ~/biz/orders 下的对象全部落在 t_abc 表
```

## 查询怎么用

映射目录照常支持 `WnQuery`（pid、regex、区间、排序、翻页），由 SqlIndexer 翻译成 SQL：

```bash
o ~/biz/orders -match 'status:"NEW",ct:[1720000000000,)' -sort 'ct:-1' -limit 20 -cqn
```

## 自动建表与分表

- `autoCreate:true` 时首次访问自动建表（字段类型来自 fields 声明）。
- 分表：`"tableName": "t_abc_${uid}"`——占位符取对象字段值，一用户一张表。

## 与 sqlx 的关系

- **形态** — dao 映射：目录=表，对象=行，**标准 WnObj 操作** — sqlx：`~/.sqlx/*.sql` 模板，**自由 SQL**
- **适合** — dao 映射：对象生命周期管理（增删改查带元数据） — sqlx：报表、联查、批量写、复杂 SQL
- **身份** — dao 映射：任何命令/脚本透明使用 — sqlx：`sqlx` 命令显式调用

两者共用 `~/.dao/` 数据源。sqlx 详见 [data-cmd](../40.commands/data-cmd.md)。

## 注意事项

- 表结构演进：改了 ix.json 的 fields 后，**自动建表不会改已存在的表**——需要手工迁移。
- `metas` 收纳列存 JSON，查询它不走 SQL 索引——高频筛选字段必须显式声明成列。
- 乐观锁：`isVersion:true` 字段在更新冲突时抛错，适合库存类场景（商城超卖检查即用它）。
