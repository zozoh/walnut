# 过滤器简介

`@break` 根据工作流上下文判断是否需要退出工作流

# 用法

```bash
wf ... @break
  [{...} ...]           # 多个条件（WnMatch），是或的关系
```

# 示例

```bash
# 如果工作流进入下一个节点，则创建一个数据集对象
$demo> wf ~/demo.json @current =obj.name @process @break '{NEXT_NAME:"[EXISTS]"}'
```
