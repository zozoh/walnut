# 命令简介

`jsonx` 用来是一个处理 JSON 的过滤器型命令
    

# 用法

```
jsonx [options] [[@filter filter-args...]...]
```

它支持的`options`有

```bash
-cqn       # JSON 格式化参数
```

它支持的过滤器有：

```bash
@read       # 从一个或多个文件读取JSON内容
@map2list   # 将一个 map 变成 list
@akeys      # 设置全局 JSON 输出的键白名单
@ikeys      # 设置全局 JSON 输出的键黑名单
@pick       # 从上下文中挑出特殊的键值
@put        # 向上下文中设置更多的值
@remove     # 从上下文中移除某些键
@get        # 从上下文获取一个值，并设置为当前对象
@nil        # 如果上下为空，设置一个默认 JSON 对象
```

本命一开始会自动从标准输入读取 JSON　内容作为初始内容。