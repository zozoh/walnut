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
SELECT * FROM ${table_name} WHERE {@vars=where} 
LIMIT {skip<int>?0}, ${limit<int>?50}

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
  [-p]           # 按照 SQL 参数模板形式预览
  [-i]           # 对于列表模式的预览，每个上下文需要打印序号
                 # 默认从 1 开始
  [-start 1]     # 对于 `-i` 模式，指定开始序号
  [-cqn]         # 对于 `-p` 模式，输出上下文的每个对象时
                 # 的 json 格式化方式
```


