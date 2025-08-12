# 过滤器简介

`@pipe` 关联上下文中的管线变量集合


# 用法

```bash
sqlx @pipe
     [{...},...]        # 将输入集合浅层设置到管线上下文
     [-view]            # 显示当前管线上下文
     [-cqn]             # 如果是显示模式时的 JSON 格式化
```
# 示例

```bash
# 显示当前管线上下文的内容
sqlx @pipe -cqn
> {...}

# 直接设置管线上下文
sqlx @pipe '{a:100,b:"hello"}' @pipe
> {a:100, b:"hello"}

# 一种更简单的写法，直接设置管线上下文
sqlx @pipe a:100  b:"hello"  'c:true' @pipe
> {a:100, b:"hello", c:true}
```

