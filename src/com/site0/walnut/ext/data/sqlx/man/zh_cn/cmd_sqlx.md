# 命令简介

`sqlx` 用来处理 SQL 数据库

它根据用户自定义的 SQL, 配合用户传入的上下文变量，可以实现对各种数据库的增删改查
等操作。

在每个用户的 `~/.sqlx` 的目录下，存放所有支持的 sql:

```bash
~/.sqlx/
|-- pet.sql
|-- address.sql
|-- food.sql
```

每个 SQL 文件实际上就是一个模板，它支持 `cmd_tmpl` 同样的语法。并在此基础上，支
持特殊占位符 `${@vars}`, 这个占位符会结合命令的上下文变量集合，输出 SQL 语句的片
段，具体的细节，请参见 `man sqlx vars`。

同时，一个 SQL 模板文件支持存放多个sql的语句模板：

```sql
------------------------------------------------
-- pet.sql
------------------------------------------------
-- 每个SQL语句之前的注释 @xxx=xx 将作为语句的元数据
-- @name=query
-- @type=select
-- @omit=a,b,c
-- @pick=x,y,z
-- @ignoreNil=true
-- 通过访问 pet.query 即可获取本条语句的模板
SELECT * FROM t_pet WHERE ${@vars=where};
-------------------------------------------------
-- @name=update
UPDATE t_pet SET${@vars=update} WHERE id=?;
```

## 上下文

整个命令生命周期内护一个上下文:

```bash
SqlxContext
|-- input  -------+ <-- # 标准输入
|                 |
|-- pipeContext --+ <-- @set|query|pipe 直接设置
|                 V
|                 V # @vars 取出之值，并设置
|                 V # 以便 @exec|query 子命令获取输入
|-- varMap -------+
|                 |
|-- varList ------+
|
|-- varMode'LIST|MAP'  # 标识 @exec 采用哪种模式的数据
#-----------------------------------------------
- input        输入上下文
- pipeContext  管线上下文
```

# 用法

# 过滤器列表

```bash
@vars       # 为当前上下文指定变量集合
@set        # 设置上下文的某个变量
@dvcheck    # 检查数据版本，防止数据同时被写入
@exec       # 执行指定的 UPDATE/DELETE/INSERT 语句
@query      # 执行指定的 SELECT 语句
@trans      # 事务相关操作
@view       # 测试 SQL 的渲染结果
@cache      # 处理 SQL 的缓存
@json       # 定义输出结果的 JSON 格式化方式
@tab        # 用表格的形式输出结果
```
