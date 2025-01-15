# 过滤器简介

`@query` 执行指定的 SQL 查询语句

这个变量集合中的变量将与模板中的占位符 `${@vars=where}` 结合输出特定的sql条件或者插入字段。
嗯，你也可以直接在模板中通过通用占位符 `${varName}` 使用这些变量。

> - 模板详细语法请参加： `man tmpl`
> - 模板的文件格式请参见： `man sqlx view`

# 用法

```bash
sqlx @query
  [sqlName]          # sql 模板的名称
  [-p]               # 采用参数模式可以避免 SQL 注入
  [-by  '{...}']     # 一个Explain表达式，循环处理查询结果
  [-set key]         # 查询的结果设置到【管道上下文】
  [-as obj|list]     # 开启模式 `-as obj` 则会仅仅挑选结果集第一个对象
                     # 并且 [-set key] 支持 explain 表达式，上下文是
                     # 原始的查询对象。 默认是 `-as list`
```


