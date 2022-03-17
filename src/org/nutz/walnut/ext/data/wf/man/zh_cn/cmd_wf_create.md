# 过滤器简介

`@create` 根据工作流上下文创建一个标准对象。

# 用法

```bash
wf ... @create
  [path/to/]        # 指定一个要创建对象的父目录
  [{...}]           # 指定要创建对象的元数据内容，可以是数组
  [-as NAME1]       # 【选】指定了这个参数，将会把这个新对象记入上下文
  [-force]          # 默认的，只有上下文中出现了 NEXT_NAME 才会执行这个过滤器
                    # 因为这表示当前工作流节点发生了改变
                    # 如果声明了 -force 选择，则无论怎样都是会执行这个过滤器
```

# 示例

```bash
# 如果工作流进入下一个节点，则创建一个标准对象
$demo> wf ~/demo.json @current =obj.name @process @create ~/tmp '{nm:"${NEXT_NAME}"}'
```
