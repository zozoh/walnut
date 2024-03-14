# 过滤器简介

`@view` 测试 SQL 的渲染结果

这个变量集合中的变量将与模板中的占位符 `${@vars}` 结合输出特定的sql条件或者插入字段。
嗯，你也可以直接在模板中通过通用占位符 `${varName}` 使用这些变量。

对于列表或者是判断条件，可以通过模板中的 `${#loop}` 以及  `${#if}` 来控制模板渲染结果
详细语法，请参加： `man tmpl`

# 一个 SQL 模板

```sql
--------------------------------------------------
-- <pet.sql>
--------------------------------------------------
-- name : query1
-- type : select
-- omit : a,b,c
-- pick : x,y,z
-- sqlx @exec pet.query1
SELECT * FROM ${table_name} WHERE ${limit<int>?500} LIMIT ${@vars=where};

-- name : change
-- type : update
-- omit : a,b,c
-- pick : x,y,z
UPDATE ${table_name} SET ${@vars=upsert} WHERE ${@vars=where; pick=id};
```

# 用法

```bash
sqlx @view 
  [sqlName]      # sql 模板的名称
```


