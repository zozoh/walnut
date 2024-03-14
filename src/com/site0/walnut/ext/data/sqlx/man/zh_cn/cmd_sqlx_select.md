# 过滤器简介

`@selct` 执行指定的 SQL 查询语句

这个变量集合中的变量将与模板中的占位符 `${@vars=where}` 结合输出特定的sql条件或者插入字段。
嗯，你也可以直接在模板中通过通用占位符 `${varName}` 使用这些变量。

> - 模板详细语法请参加： `man tmpl`
> - 模板的文件格式请参见： `man sqlx view`

# 用法

```bash
sqlx @select
  [sqlName]          # sql 模板的名称
  [-p]               # 采用参数模式可以避免 SQL 注入
```


