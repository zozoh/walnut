# 过滤器简介

`@macro` 对文本做宏展开（如 `$date:now`），结果直接输出，不参与 `@gen` 的值生成。

# 用法

```bash
val @macro 文本 [-n]
  -n    # 输出后不追加换行
```

# 示例

```bash
# 展开日期宏
val @macro '今天是 $date:now'

# 不换行输出
val @macro -n '$date:now'
```
