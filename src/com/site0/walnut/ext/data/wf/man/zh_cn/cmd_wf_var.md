# 过滤器简介

`@var` 为上下文设置变量

# 用法

```bash
wf ... @var
  [name]             # 指明变量名称，如果不指定名称
                     # 则会将目标对象所有的键作为变量名批量加入
                     # 如果加载目标是一个集合，
                     # 则会认为是 {list:[], count:8} 形式的对象
  [{..} ...]         # 后续参数多个为变量集合
  [-f /path/to]      # 加载对象内容为变量（可被 explain，上下文为当前var集合）
  [-o /path/to]      # 加载对象元数据为变量（可被 explain，上下文为当前var集合）
  [-select]          # 传入的不是变量集合，而是一个挑选模式的数组
                     # [{test:WnMatch, value:Any}...]
                     # 如果开启了选择模式，那么 -o 以及 -pick 参数会被无视
                     # 并且，如果没有 name，那么选择的值必须是一个 Map
  [-pick AutoMatch]  # 加载前，过滤对象的键，枚举或者正则都支持
```

# 示例

```bash
$demo> wf @var -f ~/abc.json
```
