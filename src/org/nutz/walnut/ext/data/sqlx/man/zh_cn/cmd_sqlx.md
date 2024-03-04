# 命令简介

`sqlx` 用来处理SQL数据库

它根据用户自定义的 SQL, 配合用户传入的上下文变量，可以实现
对各种数据库的增删改查等操作。

在每个用户的 `~/.sqlx` 的目录下，存放所有支持的 sql:

```bash
~/.sqlx/
|-- pet.add.sql
|-- pet.update.sql
|-- pet.query.sql
|-- pet.delete.sql

#-----------------------
# 例如 pet.query.sql哼哼哼哼哼哼。
#-----------------------
SELECT * FROM t_pet LIMIT ${limit<int>?100},${skip<int>?0}
```

每个 SQL 文件实际上就是一个模板，它支持 `cmd_tmpl` 同样的语法。
并在此基础上，支持特殊占位符 `${@vars}`, 这个占位符会结合命令的
上下文变量集合，输出 SQL 语句的片段：

```bash
# 假设上下文变量为
vars = {name:"xiaobai", race:"cat", color:"white", age:6}

# ${@vars=upsert; omit=^(race)}
# 它将会从上下文变量中提取除了  race 以外所有的变量并生成
# 适合 update 以及 insert 的字段列表：
SQL Template: 
    name=?, color=?, age=?

Prepare Statement:
    ["xiaobai", "white", 6]

# ${@vars=where; pick=^(name|color|age)}
# 它将会从上下文变量中提取 name|color|age 这三个变量并生成:
SQL Template: 
    name=? AND color=? AND age=?

Prepare Statement:
    ["xiaobai", "white", 6]
```

如果 `${@vars}` 相当于  `${@vars:auto}`, 即，命令会根据当前的操作
 *(由 `@exec` 来指定的操作类型: `select|update|delete|insert|exec`)*
 来自动决定是 `upsert` 还是 `where`. 它的逻辑也很简单：

 - `upsert` : `update | insert`
 - `where` | `select|delete|exec`

 同时，在 `where` 模式，为了更加方便的支持丰富的条件选择模式，它兼容
 `o @query` 同款语法，即：

 
 - `xx:"[3,100)"`  : *数字区间* : `xx>=? AND xx<?`&[3,100]
 - `xx:"^ABC"`     : *正则匹配* : `xx REGEXP ?`&[^ABC]
 - `xx:"*A?C"`     : *模糊匹配* : `xx LIKE ?`&[%A_C]
 - `xx:null`       : *空匹配*   : `xx IS NULL`
 - `xx:""`         : *存在匹配* : `xx IS NOT NULL`
 - `xx:[v1,v2]`    : *枚举匹配* : `xx IN (?,?)`&[v1,v2]
 - `xx:{..}`       : *高级匹配* : 这里类似 Mongo 支持 `$gt, $lt ...` 等
 - `!{exp}`        : *取反匹配* : `NOT {EXP}`

同时 SQL 模板文件支持文件头

```sql
/*
# 文件开头的多行注释，为文件头，文件头里支持 # 开头的行注释
# 之后，就是一个 JSON 表示文件的元数据
{
    # 本语句的SQL操作，它覆盖自动模式
    type: "select",
    # 默认的挑选变量
    pick: ["f1","f2"],
    # 默认的忽略变量
    omit: ["f1","f2"]
}
*/
```

# 用法


# 过滤器列表

```bash
@vars       # 为当前上下文指定变量集合
@exec       # 执行指定的 SQL
@trans      # 事务相关操作
@json       # 定义输出结果的 JSON 格式化方式
```



