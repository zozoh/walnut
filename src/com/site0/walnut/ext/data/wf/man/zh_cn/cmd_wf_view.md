# 过滤器简介

`@view` 查看当前上下文中的信息


# 用法

```bash
wf ... @view 
  [vars]              # 指定查看上下文中的哪些信息
                      #  vars: 查看变量
                      #  data: 查看工作流全部信息
                      #  node: 查看工作流节点信息
                      #  edge: 查看工作流边信息
                      # 如果不指定，则返回全部上下文信息
  [-cqn]              # 对于变量等输出的 JSON 格式
```

# 示例

```bash
# 查看工作流的上下文
$demo> wf ~/demo.json @current =obj.name Peter @view
```
