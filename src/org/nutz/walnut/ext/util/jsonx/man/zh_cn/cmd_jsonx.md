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
@akeys      # 设置全局 JSON 输出的键白名单
@filter     # 保留符合过滤条件的上下文集合中的对象
@omit       # 移除符合过滤条件的上下文集合中的对象
@get        # 根据一个键路径取值
@list2map   # 将一个列表转换为Map
@lock       # 指定输出时，锁定不输出的字段
@map2list   # 将一个Map转换为列表
@merge      # 将输入的JSON合并到上下文中
@pick       # 从上下文中挑出特殊的键值
@put        # 向上下文中设置更多的值
@read       # 从一个或多个文件读取JSON内容
@remove     # 从上下文中移除某些键
@set        # 将输入的JSON设置到上下文中（非合并）
@tab        # 将上下文按照表格输出
@nil        # 如果上下为空，设置一个默认 JSON 对象
```

本命一开始会自动从标准输入读取 JSON　内容作为初始内容。