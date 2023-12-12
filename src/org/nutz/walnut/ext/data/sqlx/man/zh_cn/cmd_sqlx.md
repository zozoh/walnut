# 命令简介

`sqlx` 用来处理SQL数据库
    
# 用法


# 过滤器列表

```bash
@exec
@select
@insert
@update
@delete
```

```bash
sqlx pet.add @vars '{name:"xiaobai", age:18}' @exec

~/.sqlx/
|-- pet.add.sql

#-----------------------
SELECT * FROM t_pet LIMIT ${limit<int>?100},${skip<int>?0}
```

```bash
sqlx begin Trans A
...
sqlx select

o @query .....

sys.exec(sqlx update)
...
sys.exec(sqlx insert)
....
sqlx @commit Trans A
```



