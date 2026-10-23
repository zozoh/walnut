# 过滤器简介

`@agg` 将上下文中的所有目录对象进行聚集操作，并输出结果。

# 用法

```bash
o @agg [聚集参数...] [-match JSON] [-tab] [-bish] [-ibase N] [-cqn]
  -match     # 过滤条件 JSON；不声明值则试图从管道读取
  -tab       # 作为表格输出
  -bish      # 表格输出的格式化开关（边框/行号/汇总/表头）
  -ibase     # 表格输出行号的起始值
  -cqn      # 默认作为 JSON 输出的格式化开关
```

## 聚集参数

聚集参数的顺序自由，过滤器根据参数的形式判断意图：

- `value=COUNT:id`：聚集计算函数与计算的键，语法 `输出字段名=[函数:]?键名`
  - 聚集函数：`COUNT|MAX|MIN|SUM|AVG`
  - 其他形式则为聚集分组键，支持转换函数：
    - `RAW`：按分组键原始值分组（默认）
    - `TIMESTAMP_TO_DATE`：分组键是时间戳，按天聚集
    - 默认规则：分组键为 `ct|lm|expi` 时用 `TIMESTAMP_TO_DATE`，否则用 `RAW`
- `xxx:ASC|DESC`：聚集结果排序，首段是输出字段名，默认 `ASC`
- `DATA100`：最多选择多少数据记录，默认不限（0 或负数也不限）
- `TOP10`：最多输出多少条结果，默认不限，特别适合 TOP10 场景

## 聚集分组键

```bash
# 多个分组键用半角逗号分隔，语法为
输出字段名=[函数:]?键名
# 譬如
name=AMS_TO_DATE:ct,type=tp

# 系统支持的转换函数
AMS_TO_DATE     # 毫秒时间戳转日期（按日统计）
AMS_TO_AHOU     # 毫秒时间戳转日期时间（精确到小时，按小时统计）
```

# 示例

```bash
# 按日期和类型统计数量，取前 10，表格输出
o ~/tmp/fakedoc @agg value=COUNT:id name=TIMESTAMP_TO_DATE:odt_create type=od_type name:DESC -tab -bish TOP10
# | name       | type      | value
# 0 | 2021-05-26 | FGSQ      | 2
# 1 | 2021-05-25 | BZTZ      | 1
# ...
# total 10 items
```
