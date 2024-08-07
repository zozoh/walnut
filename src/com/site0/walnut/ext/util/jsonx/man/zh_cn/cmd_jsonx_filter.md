# 过滤器简介

`@filter` 将上下文的列表进行过滤。输出一个过滤后的列表。

本过滤器假想两种过滤模式:

1. *它过滤* ：自己(上下文列表)是数据，参数里的是条件，能被条件匹配的数据项目才会输出
1. *自过滤* : 自己(上下文列表)是条件，参数里的是被匹配数据，能匹配这个数据的项目才会输出

# 用法

## `它过滤`模式

```bash
@filter 
  [WnMatch or Path ...]  # 直接指定了过滤条件。(符合 WnMatch 语法的 JSON)
  [-and]                 # 指明多个键判断关系是 AND，默认为 OR
  [-f]                   # 指明参数是文件路径，条件存放在文件内容里
  [-meta]                # 指明参数是文件路径，条件存放在文件元数据里；比 `-f` 优先
```

## `自过滤`模式

```bash
@filter 
  [-self Json or Path]   # 开启自过滤模式，指明一个上下文内容或者文件路径
  [KEY ...]              # 指定多个键，每个键表示列表元素的过滤条件
                         #  - 普通键: 直接从列表元素取值，转换为 WnMatch
                         #    + age  直接取列表元素的 age 字段
                         #  - 压值键: 从列表元素取值后压入一个 Map 后再转换为 WnMatch
                         #    + age:my_age 取列表元素的 age 字段并成为 {my_age:$age}
  [-and]                 # 指明多个键判断关系是 AND，默认为 OR
  [-f]                   # 指明self参数是文件路径，条件存放在文件内容里
  [-meta]                # 指明self参数是文件路径，条件存放在文件元数据里；比 `-f` 优先
```

# 示例

```bash
#----------------------------------------------
# 它过滤
#----------------------------------------------
# 过滤输出所有 x 大于等于 80 的项目
echo '[{x:100},{x:80},{x:50}]' | jsonx -cqn @filter '{x:"[80,)"}'
> [{"x":100}, {"x":80}]

#----------------------------------------------
# 自过滤
#----------------------------------------------

```

