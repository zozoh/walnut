---
title: 查询与聚合
module: api.io
keywords: [WnQuery, 查询, agg, 聚合, 分组, 排序, 翻页, match]
source:
  - api/io/WnQuery.java
  - api/io/agg/
  - core/indexer/mongo/
related: [everything-is-obj, dao-mapping, file-obj]
---

# 查询与聚合

## 查询：`o` 命令的 `-match`

`WnQuery` 是所有索引器统一实现的查询抽象。命令行入口是 `o` 的 `-match`（简写 `-m`）：

```bash
o ~/orders -match 'status:"NEW"' -cqn                    # 相等
o ~/orders -match 'ct:[1720000000000,1721000000000)' -cqn  # 区间（数学区间语法）
o ~/orders -match 'nm:"^order_"' -cqn                    # 正则（字符串默认按正则理解，视索引器而定）
o ~/orders -match 'lbls:"vip"' -cqn                     # 标签数组包含
o ~/orders -match 'status:"NEW",ct:[..,)' -sort 'ct:-1' -limit 20 -skip 0 -cqn
```

查询条件对象语法（多处复用：match、hook_by、cache-test）：

- **`"^abc$"`**：`^` 开头=正则
- **`"!^abc"`**：`!` 前缀=取反
- **`"hello Mr. *"`**：`*`=通配符
- **`"~~abc"`**：无视大小写精确匹配
- **`[1,100)` / `(,100]`**：数字/日期区间（见 [动态校验语法](../50.regapi/auth-history.md)）
- **`null` / `!KEY:null`**：存在性判断
- **`""` / `[BLANK]`**：空串 / 空白串判断
- **`true`**：布尔

> 完整语义见 [auth-history](../50.regapi/auth-history.md) 的验证语法一节（c2-sxm 体系）。

## 排序与翻页

```bash
-sort 'ct:-1,nm:1'   # -1 降序，1 升序；多字段按序
-limit 20 -skip 0     # 翻页（skip=页码×页大小）
-cqn                  # 输出控制：紧凑/不加引号/忽略 null
-bish                 # 表格输出（列由 -t 指定）
```

## 聚合：agg

按条件分组 + 聚合函数（count/sum/max/min/avg…），由 `agg` 参数或 `o` 命令族实现，MongoIndexer 翻译成聚合管道，SqlIndexer 翻译成 GROUP BY。

```bash
# 按状态统计订单数（示意，具体参数见 man o 的 agg 相关页）
o ~/orders -match 'ct:[..,)' -agg 'group:status,func:count'
```

聚合要素（`api/io/agg/`）：

- **`WnAggGroupKey`**：分组键，支持字段、日期截断（按天/按月）、范围桶
- **`WnAggFunc`**：聚合函数：count/sum/avg/max/min/first/last
- **`WnAggOptions`**：整体选项：条件、分组、排序、翻页
- **`WnAggTransMode`**：结果形态：树形/扁平/表格

## 编程接口

```java
WnQuery q = Wn.Q.pid(oDirId).match("status", "NEW")
                       .sortBy("ct", -1)
                       .limit(20).skip(0);
List<WnObj> list = io.query(q);

WnAggOptions agg = new WnAggOptions();
agg.setQuery(q);
agg.addGroupKey("status");
agg.addFunc("count");
WnAggResult re = io.aggregate(q, agg);
```

## 注意事项

- 不同索引器的能力不同：mongo 最全；redis 只支持按 ID 取；mem 是傻查询。
- 正则查询在 mongo 上走索引的能力有限（前缀正则可用索引）；海量数据用 dao 映射 + 显式索引列。
- 区间语法闭开区间别写反：`[a,b)` 含 a 不含 b。
