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
#
# 全局设置 
#
@akeys      # 设置全局 JSON 输出的键白名单
@ikeys      # 指定输出时，锁定不输出的字段
#
# 操作
#
@list2map   # 将一个列表转换为Map
@map2list   # 将一个Map转换为列表
@merge      # 将输入的JSON合并到上下文中
@put        # 向上下文中设置更多的值
@remove     # 从上下文中移除某些键
@append     # 假设上下文为列表，向其尾部压入更多对象
@prepend    # 假设上下文为列表，向其首部压入更多对象
@set        # 将输入的JSON设置到上下文中（非合并）
@nil        # 如果上下为空，设置一个默认 JSON 对象
@flatten    # 将一个树型结构的 JSON 数据转换为一个列表
@validate   # 检查对象的键是否符合约束
@transkey   # 转换上下文对象的键
@translate  # 转换上下文对象的值
@match      # 判断上下文是否符合指定条件
#
# 读取&过滤
#
@get        # 根据一个键路径取值
@pick       # 从上下文中挑出特殊的键值
@read       # 从一个或多个文件读取JSON内容
@filter     # 保留符合过滤条件的上下文集合中的对象
@omit       # 移除符合过滤条件的上下文集合中的对象
@item       # 从上下文列表中获取一个元素
#
# 读取&过滤
#
@tab        # 将上下文按照表格输出
@tmpl       # 根据一个字符串模板，渲染输出
```

本命一开始会自动从标准输入读取 JSON　内容作为初始内容。