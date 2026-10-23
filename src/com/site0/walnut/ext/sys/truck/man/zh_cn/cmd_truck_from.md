# 过滤器简介

`@from` 设置转换源。

# 用法

```bash
truck @from 目录 [-index 名] [-bm 名] [-match JSON] [-sort JSON] [-limit N] [-skip N]
       # 目录     # 必须为一个目录
  -index    # 指明索引映射方式，默认采用目录上的原始设定
  -bm       # 指明桶管理器，默认采用目录上的原始设定
  -match    # 指明一个过滤条件 JSON
  -sort     # 指明一个排序方式 JSON（如 '{ct:-1}'），以便固定顺序分页
  -limit    # 最大数据量，默认 1000
  -skip     # 跳过多少条数据开始，默认 0
```

# 示例

```bash
# 从 dao(abc) 数据源读取，转换到当前目录
truck @from ~/account -index dao(abc) @to

# 带条件和排序
truck @from ~/account -match '{age:{$gt:10}}' -sort '{ct:-1}' @to
```
