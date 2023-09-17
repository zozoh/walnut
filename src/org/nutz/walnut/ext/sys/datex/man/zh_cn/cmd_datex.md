# 命令简介

`datex` 处理日期时间相关操作

它的上下文永远是是一个 `Calendar` 对象

# 用法

```
datex [[@filter filter-args...]...]
```

它支持的过滤器有：

```bash
@set      # 为上下文设置一个日期时间
@time     # 为上下文设置时间
@month    # 修改日期的月份
@day      # 修改日期的天，支持工作日和节假日模式
@format   # 指定日期格式化方式
```
