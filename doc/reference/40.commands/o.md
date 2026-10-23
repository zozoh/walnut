---
title: o 命令：对象操作瑞士军刀
module: 40.commands
keywords: [o, 对象操作, @query, @update, @agg, 上下文, 过滤器]
source: [ext/data/o/]
related: [03_cmd_filter, ../20.io-storage/query-agg.md, ../10.concepts/02-std-obj.md]
---

# o 命令：对象操作瑞士军刀

`o` 是 Walnut 里使用频率最高的命令：对系统对象的增删改查、聚合统计、树形整理、批量修改，全部通过一个**上下文 + 过滤器管道**完成。

- 源码：`src/com/site0/walnut/ext/data/o/`（`OContext` 维护上下文，`hdl/` 是 39 个子命令）
- 手册：`man o` 及 38 个子页（`man o query` 等）
- 它取代了旧的内置 `obj` 命令（`cmd_obj` 已弃用），文档与脚本请统一用 `o`

## 设计思路

三个核心理念：

1. **一切皆对象**：`o` 的操作对象就是文件系统里的对象（文档、图片、订单、用户……都是对象），命令本身不关心业务语义；
2. **上下文贯穿**：命令运行期间维护一个 `OContext`——当前对象列表 + 变量。过滤器逐个加工这个上下文（`@query` 填充列表 → `@update` 修改 → `@json` 输出），像流水线一样组合；
3. **子命令即函数**：`@query/@get/@update/...` 是可任意组合的原子操作，复杂行为靠组合而不是巨型参数集。

对比一下两种写法：

```bash
# 旧 obj：一个大命令，参数决定一切
o ~/orders -match 'status:"NEW"' -sort 'ct:-1' -limit 10 -u 'tp:"X"'

# o：管道组合，每一步语义清晰
o ~/orders @query '{status:"NEW"}' -sort '{ct:-1}' -limit 10 @update 'tp:"X"' @json -cqn
```

## 适用场景

- **数据 CRUD**：任何「记录=对象」的数据操作（订单、文章、配置）
- **元数据批量治理**：改标签、补字段、按模板批量执行
- **统计报表**：`@count / @agg / @summary` 三级聚合
- **树形数据整理**：`@tree/@flat/@children/@ancestors`
- **关联查询**：`@join_one/@join_many` 把别的对象嵌入结果

## 用法：按流程分组

### 定位对象（入口）

```bash
o ~/orders @query '{status:"NEW"}' -sort '{ct:-1}' -limit 10   # 条件查询（多个条件为 OR）
o @get id:45g..67q                                              # 按 ID
o ~/orders/4k2.. @fetch                                          # 按路径（可多个）
o @create ~/orders '{nm:"order_9",status:"NEW"}'                 # 创建
```

### 加工上下文

```bash
o ~/orders @query '{status:"NEW"}' -l \
   @children -depth 2                    # 递归取子节点
   @refer                                 # 读出关联对象（如外键详情）
   @join_one /home/demo/users -query '{id:"${buyer_id}"}' -to buyer   # 嵌入买家信息
```

### 修改

```bash
o ~/a.txt @update 'brief:"新摘要",lbls:["x"]'
o ~/orders @inc 1 -key nb -quiet          # 对上下文对象的原子序号 +1（发号器）
o ~/tmp @query '{expi_at:{$lt:1710000000000}}' -l @delete
```

### 聚合统计

```bash
o ~/orders @count '{status:"NEW"}'                     # 计数
o @agg ~/orders value=COUNT:id -match '{status:"NEW"}'  # 目录聚集（分组/函数见 man o agg）
o ~/orders @query -l @summary -cqn                     # 上下文汇总统计
```

### 输出

```bash
o ~/orders @query '{status:"NEW"}' -l @json -cqn        # JSON
o ~/orders @query '{status:"NEW"}' -l @tab              # 表格
o ~/orders @query '{status:"NEW"}' -l @tmpl '${nm} -> ${status}\n'   # 行模板
o ~/photos @query -l @zip ~/photos.zip                   # 打包下载
```

### 上下文变量与批量

```bash
o ~/orders @var '{discount:0.8}' \
   @query -l \
   @update 'final_fee:${it.fee * discount}'      # 用变量计算
```

## 典型组合示例

**「找出 7 天没更新的订单，打标签并通知」**

```bash
o ~/orders @query '{status:"NEW", lm:{$lt:七天前}}' -l \
   @update 'lbls:["stale"]' \
   @tmpl 'echo "催单 ${nm}" | mq send;\n' | run
```

**「统计各状态订单数和金额」**

```bash
o @agg ~/orders value=COUNT:id value=SUM:fee -match '{race:"FILE"}'   # 分组维度见 man o agg
```

**「对象树导出给前端」**

```bash
o ~/categories @tree -subkey children @json -cqn
```

## 注意事项

- `@query` 的 `-match` 语法与 WnQuery 一致（见 [查询与聚合](../20.io-storage/query-agg.md)）；
- 默认只输出一个对象时不带列表包裹，加 `-l` 强制列表输出（脚本里建议永远加）；
- `@inc` 是原子操作，适合发号器场景（订单号、ID）；
- 批量操作前先 `@query ... @json` 预览命中集，确认无误再 `@update/@delete`。

## 延伸阅读

- [过滤命令](03_cmd_filter.md) — 过滤器管道机制
- [一切皆对象](../10.concepts/02-std-obj.md)
- [查询与聚合](../20.io-storage/query-agg.md)
