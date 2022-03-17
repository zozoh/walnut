# 过滤器简介

`@current` 根据上下文变量，确定工作流当前节点的名称。

它接受一系列参数，每个参数都是一个`explain`语法，它将第一个执行结果不为空的参数作为当前节点名称。其中 `explain` 语法的上下文是当前命令的变量集。当前节点名称记录在上下文`CURRENT_NAME`变量中。

# 用法

```bash
wf ... @current [EXPLAIN...]
```

# 示例

```bash
# 尝试从上下文中寻找 obj.name 如果没有，就用 'Peter' 字符串作为当前节点名称
$demo> wf ~/demo.json @current =obj.name Peter
```
