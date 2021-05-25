# 过滤器简介

`@count` 汇总指定条件下的对象数量

# 用法

```bash
o @count
  [{..}]          # 可多个条件，为 OR 的关系
  [-p ~/xxx]      # 指定一个父节点
  [-mine]         # 为条件添加 d0:"home", d1:"主组" 两条约束
  [-skip 0]       # 跳过多少条记录
  [-limit 10]     # 最多多少条记录
  [-as count]     # 汇总键名，默认为 count
                  # 如果不指定本参数，则直接输出一个数字
```

# 示例

```bash
$demo:> o ~/mydir @count -as toddy '{ct:"[today,today+1d)"}' @summary -cqn
{"toddy":4}
```

