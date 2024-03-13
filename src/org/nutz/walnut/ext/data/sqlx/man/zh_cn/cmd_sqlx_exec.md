# 过滤器简介

`@exec` 执行指定的 SQL

这个变量集合中的变量将与模板中的占位符 `${@vars}` 结合输出特定的sql条件或者插入字段。
嗯，你也可以直接在模板中通过通用占位符 `${varName}` 使用这些变量。

对于列表或者是判断条件，可以通过模板中的 `${#loop}` 以及  `${#if}` 来控制模板渲染结果
详细语法，请参加： `man tmpl`

# 一个 SQL 模板

```sql
--------------------------------------------------
-- <pet.sql>
--------------------------------------------------
-- @name = query
-- @type = select
-- @omit = limit,skip
-- > sqlx @select pet.query
SELECT * FROM t_pet WHERE ${@vars=where}; LIMIT ${skip<int>?0}, ${limit<int>?50};
--------------------------------------------------
-- @name = change
-- @type = update
-- @omit = a,b,c
-- @pick = x,y,z
UPDATE t_pet SET ${@vars=upsert} WHERE ${@vars=where; pick=id};
```

# 用法

```bash
sqlx @exec 
  [sqlName]          # sql 模板的名称
  [-select]          # 执行的类型:
                     #  -select
                     #  -update
                     #  -insert
                     #  -delete
                     #  -exec
  [-batch]           # 尽在 update/insert 时有效，表示批量操作
                     # 当然上下文对象，必须是一个列表
```


