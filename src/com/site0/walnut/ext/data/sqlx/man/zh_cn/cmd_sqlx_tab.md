# 过滤器简介

`@tab` 把 `sqlx` 的结果按指定字段输出为表格。

# 用法

```bash
sqlx @tab
  [field ...]       # 要输出的列名，空格分隔；也可以分成多个参数
  [-b]              # 显示边框
  [-h]              # 显示表头
  [-s]              # 显示汇总行
  [-i]              # 显示行号
  [-ibase 0]        # 行号起始值
```

- 必须给至少一个字段；字段顺序就是列顺序
- 结果来自前序 `@query` / `@exec` 的 `result`
- 输出表格后不会再输出原始 JSON

# 示例

```bash
# 两列查询结果
sqlx @vars '{filter:{st:1}}' @query pet.select @tab id code -h -b

# 带行号
sqlx @vars '{filter:{st:1}}' @query pet.select @tab id code -i -ibase 1
```
