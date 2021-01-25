# 过滤器简介

`@query` 查询出一组对象

如果上下文有内容，会用第一个对象作为父ID

# 用法

```bash
o @query
  [{..}]          # 可多个条件，为 OR 的关系
  [-mine]         # 为条件添加 d0:"home", d1:"主组" 两条约束
  [-pager]        # 表示要记录翻页信息
  [-sort {ct:1}]  # 排序方式
  [-skip 0]       # 跳过多少条记录
  [-limit 10]     # 最多多少条记录
  [-append]       # 表示本次查询结果不会覆盖上下文
                  # 而是直接添加到结果列表后面
```

# 示例

```bash
o ~/accounts/index @query 'nm:"xiaobai"'
```

